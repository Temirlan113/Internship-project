package com.kz.internship_project.dto.lesson;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Запрос на создание урока")
public record LessonCreateDto(

        @Schema(description = "Название урока", example = "Строки в Java")
        @NotBlank(message = "Название урока не может быть пустым")
        String name,

        @Schema(description = "Описание урока", example = "Строки в Java - это объекты, которые хранят текст...")
        @NotBlank(message = "Описание урока не может быть пустым")
        String description,

        @Schema(description = "Содержание урока", example = "Строка представляет собой последовательность символов...")
        @NotBlank(message = "Содержание урока не может быть пустым")
        String content,

        @Schema(description = "ID главы, к которой относится урок", example = "1")
        @NotNull(message = "ID главы не может быть пустым")
        @Positive(message = "ID главы должен быть положительным числом")
        Long chapterId
) {}