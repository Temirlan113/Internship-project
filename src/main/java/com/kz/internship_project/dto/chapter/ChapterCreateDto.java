package com.kz.internship_project.dto.chapter;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Запрос на создание главы курса")
public record ChapterCreateDto(

        @Schema(description = "Название главы", example = "Переменные Java")
        @NotBlank(message = "Название главы не может быть пустым")
        String name,

        @Schema(description = "Описание главы", example = "Для хранения данных в программе предназначены переменные...")
        @NotBlank(message = "Описание главы не может быть пустым")
        String description,

        @Schema(description = "ID курса, к которому относится глава", example = "1")
        @NotNull(message = "ID курса не может быть пустым")
        @Positive(message = "ID курса должен быть положительным числом")
        Long courseId
) {}