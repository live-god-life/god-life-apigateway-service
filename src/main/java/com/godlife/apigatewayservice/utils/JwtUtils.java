package com.godlife.apigatewayservice.utils;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Slf4j
public class JwtUtils {

    /**
     * JWT token 암호화 키
     */
    private static String secretKey;

    @Value("${jwt.secretKey}")
    private void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    /**
     * JWT token 유효성 검사
     * @param jwt   JWT token
     * @return 토큰 유효성 검사 결과
     */
    public static boolean isJwtValid(String jwt) {
        String subject;

        try {
            subject = Jwts.parser()
                          .setSigningKey(secretKey.getBytes())
                          .parseClaimsJws(jwt)
                          .getBody()
                          .getSubject();
        } catch (MalformedJwtException | SignatureException e) {
            log.error("Invalid jwt signature");
            return false;
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token");
            return false;
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token");
            return false;
        } catch (Exception e) {
            return false;
        }

        return StringUtils.hasText(subject);
    }

    /**
     * JWT token 생성
     * @param request       ServerHttpRequest
     * @return JWT token
     */
    public static String getToken(ServerHttpRequest request) {
        return Optional.ofNullable(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                       .filter(token -> token.indexOf("Bearer") > -1)
                       .map(token -> token.substring("Bearer ".length()))
                       .map(String::trim)
                       .orElse(null);
    }
}
