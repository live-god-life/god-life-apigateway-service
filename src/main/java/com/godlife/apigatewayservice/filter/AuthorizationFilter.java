package com.godlife.apigatewayservice.filter;

import com.godlife.apigatewayservice.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AuthorizationFilter extends AbstractGatewayFilterFactory<AuthorizationFilter.Config> {

    /**
     * AuthorizationFilter 생성자
     */
    public AuthorizationFilter() {
        super(Config.class);
    }

    /**
     * 설정 관련 config
     */
    public static class Config {}

    /**
     * 필터 실행 로직
     * @param config    설정 내용
     * @return 다음 필터 진행
     */
    @Override
    public GatewayFilter apply(Config config) {
        return (((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            String jwt = JwtUtils.getToken(request);

            // 헤더에 토큰이 없는 경우
            if(!StringUtils.hasText(jwt)) {
                return onError(exchange, "No authorization header", HttpStatus.UNAUTHORIZED);
            }

            // 토큰이 유효하지 않는 경우
            if(!JwtUtils.isJwtValid(jwt)) {
                return onError(exchange, "JWT token is not valid", HttpStatus.UNAUTHORIZED);
            }

            // 토큰에서 사용자 정보 추출
            String userId = JwtUtils.extractTokenToUserId(jwt);

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
     * @param errorMessage      에러 메시지
     * @param httpStatus        Http 상태 코드
     * @return 에러 Response
     */
    private Mono<Void> onError(ServerWebExchange exchange, String errorMessage, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);

        if(log.isErrorEnabled()) {
            log.error("Error code: {}", httpStatus);
            log.error("Error message: {}", errorMessage);
        }

        return response.setComplete();
    }
}
