package com.kz.internship_project.dto;


import java.time.LocalDateTime;

public record CourseResponseDto(Long id, String name, String description, LocalDateTime createdTime, LocalDateTime updatedTime) {


}
