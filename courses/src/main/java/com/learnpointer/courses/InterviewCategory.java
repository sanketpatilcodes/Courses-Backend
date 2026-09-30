package com.learnpointer.courses;

import java.util.List;

public record InterviewCategory(int id, String name, List<InterviewQuestion> questions) {
}