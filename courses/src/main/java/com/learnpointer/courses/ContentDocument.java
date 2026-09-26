package com.learnpointer.courses;

import java.util.List;

public record ContentDocument(String title, String description, List<ContentItem> tutorials) {
}