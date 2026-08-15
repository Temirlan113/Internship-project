package com.kz.internship_project.dto;


import java.time.LocalDateTime;

public record ChapterResponseDto(Long id, String name, String description, int chapterOrder, Long courseId,
                                 LocalDateTime createdTime, LocalDateTime updatedTime) {

}
