package com.learnpointer.courses;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaTypeFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/content")
public class ContentController {

	private final TutorialContentService contentService;

	public ContentController(TutorialContentService contentService) {
		this.contentService = contentService;
	}

	@GetMapping
	public ApiResponse<List<ContentSection>> getContentCatalog() {
		List<ContentSection> content = contentService.findContentSections();
		return ApiResponse.success(200, "Content catalog loaded", content);
	}

	@GetMapping("/{courseSlug}")
	public ApiResponse<List<ContentCourse>> getCourses(@PathVariable String courseSlug) {
		return ApiResponse.success(200, "Courses loaded", contentService.findCourses(courseSlug));
	}

	@GetMapping("/{sectionSlug}/{courseSlug}")
	public ApiResponse<ContentGroup> getCourse(@PathVariable String sectionSlug, @PathVariable String courseSlug) {
		return ApiResponse.success(200, "Course loaded", contentService.findContentGroup(sectionSlug, courseSlug));
	}

	@GetMapping(value = "/{courseSlug}/content.json", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Resource> getContentFile(@PathVariable String courseSlug) {
		return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
				.body(contentService.contentFile(courseSlug));
	}

	@GetMapping(value = "/{sectionSlug}/{courseSlug}/content.json", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Resource> getContentFile(@PathVariable String sectionSlug, @PathVariable String courseSlug) {
		return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
				.body(contentService.contentFile(sectionSlug, courseSlug));
	}

	@GetMapping("/{sectionSlug}/{courseSlug}/{slug}")
	public ApiResponse<Tutorial> getTutorial(@PathVariable String sectionSlug, @PathVariable String courseSlug,
			@PathVariable String slug) {
		return ApiResponse.success(200, "Tutorial loaded", contentService.findBySlug(sectionSlug, courseSlug, slug));
	}

	@GetMapping("/{courseSlug}/{slug}/image")
	public ResponseEntity<Resource> getTutorialImage(@PathVariable String courseSlug, @PathVariable String slug) {
		Tutorial tutorial = contentService.findBySlug(courseSlug, slug);
		Resource image = contentService.imageFor(courseSlug, slug);
		MediaType mediaType = MediaTypeFactory.getMediaType(tutorial.fileName())
				.orElse(MediaType.APPLICATION_OCTET_STREAM);
		return ResponseEntity.ok().contentType(mediaType).body(image);
	}

	@GetMapping("/{sectionSlug}/{courseSlug}/{slug}/image")
	public ResponseEntity<Resource> getTutorialImage(@PathVariable String sectionSlug, @PathVariable String courseSlug,
			@PathVariable String slug) {
		Tutorial tutorial = contentService.findBySlug(sectionSlug, courseSlug, slug);
		Resource image = contentService.imageFor(sectionSlug, courseSlug, slug);
		MediaType mediaType = MediaTypeFactory.getMediaType(tutorial.fileName())
				.orElse(MediaType.APPLICATION_OCTET_STREAM);
		return ResponseEntity.ok().contentType(mediaType).body(image);
	}
}