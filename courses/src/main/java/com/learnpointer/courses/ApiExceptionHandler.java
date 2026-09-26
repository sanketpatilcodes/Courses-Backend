package com.learnpointer.courses;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ApiResponse<Map<String, Object>>> handleResponseStatus(ResponseStatusException exception) {
		HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
		return error(status, exception.getReason() == null ? "Request failed" : exception.getReason());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Map<String, Object>>> handleUnexpected(Exception exception) {
		return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
	}

	private ResponseEntity<ApiResponse<Map<String, Object>>> error(HttpStatus status, String message) {
		Map<String, Object> details = new LinkedHashMap<>();
		details.put("timestamp", Instant.now());
		details.put("path", "See request URL");
		return ResponseEntity.status(status)
				.body(new ApiResponse<>(false, status.value(), message, details));
	}
}