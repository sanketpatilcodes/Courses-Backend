package com.learnpointer.courses;

public record ApiResponse<T>(boolean success, int status, String message, T data) {

	public static <T> ApiResponse<T> success(int status, String message, T data) {
		return new ApiResponse<>(true, status, message, data);
	}
}