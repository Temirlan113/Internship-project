package com.kz.internship_project.dto;

import com.kz.internship_project.entity.Chapter;

import java.time.LocalDateTime;

public record LessonResponseDto(Long id, String name, String description, String content, int lessonOrder, Long chapterId,
                                LocalDateTime createdTime, LocalDateTime updatedTime) {
}
