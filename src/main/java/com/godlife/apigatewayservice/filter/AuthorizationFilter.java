package com.godlife.apigatewayservice.filter;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

import com.godlife.apigatewayservice.response.ApiResponse;
import com.godlife.apigatewayservice.utils.JwtUtils;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AuthorizationFilter extends AbstractGatewayFilterFactory<AuthorizationFilter.Config> {

	@Autowired
	private LoadBalancerClient loadBalancerClient;

	/**
	 * AuthorizationFilter 생성자
	 */
	public AuthorizationFilter() {
		super(Config.class);
	}

	/**
	 * 설정 관련 config
	 */
	public static class Config {
	}



	/**
	 * 필터 실행 로직
	 * @param config    설정 내용
	 * @return 다음 필터 진행
	 */
	@Override
	public GatewayFilter apply(Config config) {

		return (((exchange, chain) -> {
			ServerHttpRequest request = exchange.getRequest();

			String jwt = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0).replace("Bearer ", "");

			// 테스트 진행용 ========================================
			if ("test".equals(jwt)) {
				// Request 헤더에 사용자 정보 추가
				ServerHttpRequest newRequest = request.mutate()
					.header("x-user", "1")
					.build();

				return chain.filter(exchange.mutate().request(newRequest).build());
			}
			// 테스트 진행용 ========================================

			// 헤더에 토큰이 없는 경우
			if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
				return onError(exchange, chain, 100, "토큰이 누락되었습니다.");
			}

			// 토큰이 유효하지 않는 경우
			if (!JwtUtils.isJwtValid(jwt)) {
				return onError(exchange, chain, 101, "토큰이 유효하지 않습니다.");
			}

			// 토큰에서 사용자 정보 추출
			String userId = JwtUtils.extractTokenToUserId(jwt);

			// 사용자 정보 유효성 검사
			WebClient webClient = WebClient.builder()
				.baseUrl(loadBalancerClient.choose("USER-SERVICE").getUri().toString())
				.build();

			Object responseData = webClient.get()
				.uri(uriBuilder -> uriBuilder
					.path("/users")
					.build())
				.header("x-user", userId)
				.retrieve()
				.bodyToMono(ApiResponse.class)
				.onErrorReturn(new ApiResponse())
				.block()
				.getData();

			if (responseData == null) {
				return onError(exchange, chain, 102, "회원이 아닙니다.");
			}

			// Request 헤더에 사용자 정보 추가
			ServerHttpRequest newRequest = request.mutate()
				.header("x-user", userId)
				.build();

			return chain.filter(exchange.mutate().request(newRequest).build());
		}));
	}

	/**
	 * 실패 시 처리 로직
	 * @param exchange          Http 요청-응답 관련 속성
	 * @param chain          	Filter Chain
	 * @param code      		에러 코드
	 * @param message        	에러 메시지
	 * @return 에러 Response
	 */
	private Mono<Void> onError(ServerWebExchange exchange, GatewayFilterChain chain, int code, String message) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(HttpStatus.UNAUTHORIZED);

		Gson gson = new Gson();
		ApiResponse<?> body = ApiResponse.createErrorCode("error", code, message);

		DataBuffer dataBuffer = response.bufferFactory().wrap(gson.toJson(body).getBytes(StandardCharsets.UTF_8));

		return response.writeWith(Flux.just(dataBuffer));
	}
}
