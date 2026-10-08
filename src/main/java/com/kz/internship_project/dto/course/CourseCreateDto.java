package com.kz.internship_project.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на создание нового курса")
public record CourseCreateDto(

        @Schema(description = "Название курса", example = "Java Core")
        @NotBlank(message = "Название курса не может быть пустым")
        @Size(max = 255, message = "Название курса не должно превышать 255 символов")
        String name,

        @Schema(description = "Описание курса", example = "Java Core — это основы языка. Под ними подразумевается целый набор понятий...")
        @NotBlank(message = "Описание курса не может быть пустым")
        @Size(max = 2550, message = "Описание курса не должно превышать 2550 символов")
        String description
) {}