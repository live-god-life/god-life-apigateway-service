package com.godlife.apigatewayservice.utils;

import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {JwtUtils.class})
public class JwtUtilsTest {

    @Test
    @DisplayName("토큰 유효성 검사를 실패한다.")
    void failValidToken() {

        // Given
        String token = "invalid-token";

        // When
        boolean isValidToken = JwtUtils.isJwtValid(token);

        // Then
        assertThat(isValidToken).isFalse();
    }

    @Test
    @DisplayName("토큰 유효성 검사를 성공한다.")
    void successValidToken() {

        // Given
        String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIwMDAxIiwiZXhwIjoxNjY3NDA0NTM0LCJpYXQiOjE2Njc0MDA5MzUsImlzcyI6InpEREo1Y1BjaFcifQ.68tQqNeNnclO_Qnog1B6d1RULz1wng9FjMOJ6vSVRf8Vbw9fHzCFiIQWw3O75ruk3gjkrwQwoh881QVa3H2UdQ";

        // When
        boolean isValidToken = JwtUtils.isJwtValid(token);

        // Then
        assertThat(isValidToken).isTrue();
    }

    @Test
    @DisplayName("토큰 추출을 실패한다.")
    void failGetJwt() {

        // Given
        String emptyToken = "";

        MockServerHttpRequest request = MockServerHttpRequest.get("/test")
                .header(HttpHeaders.AUTHORIZATION, emptyToken)
                .build();

        // When
        String token = JwtUtils.getToken(request);

        // Then
        assertThat(token).isNull();
    }

    @Test
    @DisplayName("토큰 추출을 성공한다.")
    void successGetJwt() {

        // Given
        String fillToken = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJsZWUiLCJleHAiOjE2NjUxNTQxMTUsImlhdCI6MTY2NTE1MDUxNSwiaXNzIjoiNWZUbEQzeWJnbiJ9.Ym8XYO47zH0iAaU8pVFRxDxLYB3UJSVdB9ZLloLoGqlul-1Xt5wT2CoJbcKLPosWhBtgdSo5k-3_mBeGHLDXIg";

        MockServerHttpRequest request = MockServerHttpRequest.get("/test")
                .header(HttpHeaders.AUTHORIZATION, fillToken)
                .build();

        // When
        String token = JwtUtils.getToken(request);

        // Then
        assertThat(token).isEqualTo("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJsZWUiLCJleHAiOjE2NjUxNTQxMTUsImlhdCI6MTY2NTE1MDUxNSwiaXNzIjoiNWZUbEQzeWJnbiJ9.Ym8XYO47zH0iAaU8pVFRxDxLYB3UJSVdB9ZLloLoGqlul-1Xt5wT2CoJbcKLPosWhBtgdSo5k-3_mBeGHLDXIg");
    }

    @Test
    @DisplayName("토큰에서 사용자 아이디 추출을 실패한다.")
    void failExtractTokenToUserId() {

        // Given
        String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJob29uIiwiZXhwIjoxODAwMDAwMDAwLCJpYXQiOjE2NjY3MDg0NDgsImlzcyI6IkJLeU5RWTdXWjIifQ.lLUhQiDlFhLYWoYKeDB8nIRvzqn4hthrrpYL39b1cdIK6X476aVthXKYPHRa2-L51XjBqh6Eq1T7Z7PYrnqcvw";

        // When
        String userId = JwtUtils.extractTokenToUserId(token);

        // Then
        assertThat(userId).isNotEqualTo("None");
    }

    @Test
    @DisplayName("토큰에서 사용자 아이디 추출을 성공한다.")
    void successExtractTokenToUserId() {

        // Given
        String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJob29uIiwiZXhwIjoxODAwMDAwMDAwLCJpYXQiOjE2NjY3MDg0NDgsImlzcyI6IkJLeU5RWTdXWjIifQ.lLUhQiDlFhLYWoYKeDB8nIRvzqn4hthrrpYL39b1cdIK6X476aVthXKYPHRa2-L51XjBqh6Eq1T7Z7PYrnqcvw";

        // When
        String userId = JwtUtils.extractTokenToUserId(token);

        // Then
        assertThat(userId).isEqualTo("hoon");
    }
}
