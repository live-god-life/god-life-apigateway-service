package com.godlife.apigatewayservice.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ApiResponse<T> {
	/** 상태 (success / error) */
	private String status;

	/** 데이터 */
	private T data;

	/** 오류 코드 */
	private Integer code;

	/** 오류 메시지 */
	private String message;

	private ApiResponse(String status, T data, Integer code, String message) {
		this.status = status;
		this.data = data;
		this.code = code;
		this.message = message;
	}

	public static ApiResponse createErrorCode(String status, Integer code, String message) {
		return new ApiResponse<>(status, null, code, message);
	}
}