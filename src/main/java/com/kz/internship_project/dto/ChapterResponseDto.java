package com.kz.internship_project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Информация о главе курса")
public record ChapterResponseDto(

        @Schema(description = "Уникальный идентификатор главы", example = "1")
        Long id,

        @Schema(description = "Название главы", example = "Переменные Java")
        String name,

        @Schema(description = "Описание главы", example = "Для хранения данных в программе предназначены переменные...")
        String description,

        @Schema(description = "Порядковый номер главы в курсе", example = "1")
        int chapterOrder,

        @Schema(description = "ID родительского курса", example = "1")
        Long courseId,

        @Schema(description = "Дата и время создания главы", example = "2026-08-17T12:00:00")
        LocalDateTime createdTime,

        @Schema(description = "Дата и время последнего обновления главы", example = "2026-08-17T14:52:00")
        LocalDateTime updatedTime
) {}