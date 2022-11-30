package com.godlife.apigatewayservice.filter;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class LoggingFilter extends AbstractGatewayFilterFactory<LoggingFilter.Config> {

	/**
	 * LoggingFilter 생성자
	 */
	public LoggingFilter() {
		super(Config.class);
	}

	/**
	 * 설정 관련 config
	 */
	@Getter
	@Setter
	public static class Config {
	}

	/**
	 * 필터 실행 로직
	 * @param config    설정 내용
	 * @return 다음 필터 진행
	 */
	public GatewayFilter apply(Config config) {
		GatewayFilter filter = new OrderedGatewayFilter(((exchange, chain) -> {
			ServerHttpRequest request = exchange.getRequest();
			ServerHttpResponse response = exchange.getResponse();
			DataBufferFactory dataBufferFactory = response.bufferFactory();

			// Request, Response 로그 출력
			ServerHttpRequest decoratedRequest = getDecoratedRequest(request);
			// ServerHttpResponseDecorator decoratedResponse = getDecoratedResponse(request, response, dataBufferFactory);

			return chain.filter(exchange.mutate().request(decoratedRequest).response(response).build());
		}), Ordered.HIGHEST_PRECEDENCE);

		return filter;
	}

	/**
	 * Request Decorator
	 * @param request    ServerHttpRequest 객체
	 * @return ServerHttpRequest 반환
	 */
	private ServerHttpRequest getDecoratedRequest(ServerHttpRequest request) {
		return new ServerHttpRequestDecorator(request) {

			@Override
			public Flux<DataBuffer> getBody() {
				log.info("<< Request Logger >>");
				log.info("Request URL: [{}] {}", request.getMethod(), request.getURI());
				log.info("Request Headers: {}", request.getHeaders());
				log.info("Request Query Parameter: {}", request.getQueryParams());

				return super.getBody().publishOn(Schedulers.boundedElastic()).doOnNext(dataBuffer -> {
					try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
						Channels.newChannel(byteArrayOutputStream).write(dataBuffer.asByteBuffer().asReadOnlyBuffer());
						String requestBody = IOUtils.toString(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8.toString());

						log.info("Request Form Data: {}", requestBody);
					} catch (Exception e) {
						log.error(e.getMessage());
					}
				});
			}
		};
	}

	/**
	 * Response Decorator
	 * @param request               ServerHttpRequest 객체
	 * @param response              ServerHttpResponse 객체
	 * @param dataBufferFactory     버퍼 팩토리
	 * @return ServerHttpResponseDecorator 반환
	 */
	ServerHttpResponseDecorator getDecoratedResponse(ServerHttpRequest request, ServerHttpResponse response, DataBufferFactory dataBufferFactory) {
		return new ServerHttpResponseDecorator(response) {

			@Override
			public Mono<Void> writeWith(final Publisher<? extends DataBuffer> body) {
				if (body instanceof Flux) {
					Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>)body;
					return super.writeWith(fluxBody.buffer().map(dataBuffers -> {
						DefaultDataBuffer joinedBuffers = new DefaultDataBufferFactory().join(dataBuffers);
						byte[] content = new byte[joinedBuffers.readableByteCount()];
						joinedBuffers.read(content);
						String responseBody = new String(content, StandardCharsets.UTF_8);

						log.info("<< Response Logger >>");
						log.info("Request URL: [{}] {}", request.getMethod(), request.getURI());
						log.info("Response Status Code: {}", response.getStatusCode());
						log.info("Response Headers: {}", response.getHeaders());
						log.info("Response body : {}", responseBody);

						return dataBufferFactory.wrap(responseBody.getBytes());
					})).onErrorResume(err -> {

						log.error("error while decorating Response: {}", err.getMessage());
						return Mono.empty();
					});

				}
				return super.writeWith(body);
			}
		};
	}
}
