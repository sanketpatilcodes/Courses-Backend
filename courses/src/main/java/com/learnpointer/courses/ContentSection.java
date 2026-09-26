package com.learnpointer.courses;

import java.util.List;

public record ContentSection(String slug, String title, List<ContentCourse> courses) {
}