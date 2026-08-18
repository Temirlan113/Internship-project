package com.kz.internship_project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Информация о курсе")
public record CourseResponseDto(

        @Schema(description = "Уникальный идентификатор курса", example = "1")
        Long id,

        @Schema(description = "Название курса", example = "Java Developer 2026")
        String name,

        @Schema(description = "Описание курса", example = "Курс по Spring Boot и Hibernate")
        String description,

        @Schema(description = "Дата и время создания курса", example = "2026-08-17T12:00:00")
        LocalDateTime createdTime,

        @Schema(description = "Дата и время последнего обновления курса", example = "2026-08-17T14:52:00")
        LocalDateTime updatedTime
) {}