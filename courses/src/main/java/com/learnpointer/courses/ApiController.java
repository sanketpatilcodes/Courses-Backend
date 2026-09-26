package com.learnpointer.courses;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {

	@GetMapping("/health")
	public ApiResponse<String> health() {
		return ApiResponse.success(200, "API is running", "ok");
	}
}