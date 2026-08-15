package com.kz.internship_project.dto;

import com.kz.internship_project.entity.Chapter;

public record LessonCreateDto(String name, String description, String content, Long chapterId) {
}
