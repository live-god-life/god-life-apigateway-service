package com.godlife.apigatewayservice.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class TokenFilter extends AbstractGatewayFilterFactory<TokenFilter.Config> {

	/**
	 * AuthorizationFilter 생성자
	 */
	public TokenFilter() {
		super(TokenFilter.Config.class);
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
	public GatewayFilter apply(TokenFilter.Config config) {

		return (((exchange, chain) -> {
			ServerHttpRequest request = exchange.getRequest();

			String jwt = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0).replace("Bearer ", "");

			// Request 헤더에 사용자 정보 추가
			ServerHttpRequest newRequest = request.mutate()
				.header(org.apache.http.HttpHeaders.AUTHORIZATION, jwt)
				.build();

			return chain.filter(exchange.mutate().request(newRequest).build());
		}));
	}
}
