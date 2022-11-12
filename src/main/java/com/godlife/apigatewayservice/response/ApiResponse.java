package com.godlife.apigatewayservice.response;

import lombok.Getter;

@Getter
public class ApiResponse<T> {
    /** 상태 (success / error) */
    private String status;

    /** 데이터 */
    private T data;

    /** 오류 코드 */
    private Integer code;

    /** 오류 메시지 */
    private String message;
}