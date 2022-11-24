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
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Slf4j
@Component
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
		} catch (UnsupportedJwtException e) {
			log.error("Unsupported JWT token");
			return false;
		} catch (ExpiredJwtException e) {
			log.error("Expired JWT token");
			return false;
		} catch (Exception e) {
			return false;
		}

		return StringUtils.hasText(subject);
	}

	/**
	 * JWT token 내부 사용자 아이디 조회
	 * @param token       JWT token
	 * @return 사용자 아이디
	 */
	public static String extractTokenToUserId(String token) {
		return Jwts.parser()
			.setSigningKey(secretKey.getBytes())
			.parseClaimsJws(token)
			.getBody()
			.getSubject();
	}
}
