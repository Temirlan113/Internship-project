package com.kz.internship_project.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Запрос на создание нового курса")
public record CourseCreateDto(

        @Schema(description = "Название курса", example = "Java Core")
        @NotBlank(message = "Название курса не может быть пустым")
        String name,

        @Schema(description = "Описание курса", example = "Java Core — это основы языка. Под ними подразумевается целый набор понятий...")
        @NotBlank(message = "Описание курса не может быть пустым")
        String description
) {}