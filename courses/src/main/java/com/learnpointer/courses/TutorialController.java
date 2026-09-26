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
@RequestMapping("/api/tutorials")
public class TutorialController {

	private final TutorialContentService contentService;

	public TutorialController(TutorialContentService contentService) {
		this.contentService = contentService;
	}

	@GetMapping
	public ApiResponse<List<Tutorial>> getTutorials() {
		return ApiResponse.success(200, "Tutorials loaded", contentService.findAll());
	}

	@GetMapping(value = "/content.json", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Resource> getContentFile() {
		return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(contentService.contentFile());
	}

	@GetMapping("/{slug}")
	public ApiResponse<Tutorial> getTutorial(@PathVariable String slug) {
		return ApiResponse.success(200, "Tutorial loaded", contentService.findBySlug(slug));
	}

	@GetMapping("/{slug}/image")
	public ResponseEntity<Resource> getTutorialImage(@PathVariable String slug) {
		Tutorial tutorial = contentService.findBySlug(slug);
		Resource image = contentService.imageFor(slug);
		MediaType mediaType = MediaTypeFactory.getMediaType(tutorial.fileName())
				.orElse(MediaType.APPLICATION_OCTET_STREAM);
		return ResponseEntity.ok().contentType(mediaType).body(image);
	}
}