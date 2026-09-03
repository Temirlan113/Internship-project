package com.kz.internship_project.dto.lesson;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Информация об уроке")
public record LessonResponseDto(

        @Schema(description = "Уникальный идентификатор урока", example = "10")
        Long id,

        @Schema(description = "Название урока", example = "Строки в Java")
        String name,

        @Schema(description = "Описание урока", example = "Строки в Java - это объекты, которые хранят текст...")
        String description,

        @Schema(description = "Полный текст / содержание урока", example = "Строка представляет собой последовательность символов...")
        String content,

        @Schema(description = "Порядковый номер урока в главе", example = "2")
        int lessonOrder,

        @Schema(description = "ID родительской главы", example = "1")
        Long chapterId,

        @Schema(description = "Дата и время создания урока", example = "2026-08-17T12:00:00")
        LocalDateTime createdTime,

        @Schema(description = "Дата и время последнего обновления урока", example = "2026-08-17T14:52:00")
        LocalDateTime updatedTime
) {}