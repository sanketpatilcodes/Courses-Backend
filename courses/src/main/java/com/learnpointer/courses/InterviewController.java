package com.learnpointer.courses;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interviews")
public class InterviewController {

	private final TutorialContentService contentService;

	public InterviewController(TutorialContentService contentService) {
		this.contentService = contentService;
	}

	@GetMapping("/{platformSlug}")
	public ApiResponse<InterviewDocument> getInterview(@PathVariable String platformSlug) {
		return ApiResponse.success(200, "Interview content loaded", contentService.findInterview(platformSlug));
	}
}