package com.learnpointer.courses;

import java.util.List;

public record ContentGroup(String sectionSlug, String slug, String title, String description,
		List<ContentItem> tutorials) {
}