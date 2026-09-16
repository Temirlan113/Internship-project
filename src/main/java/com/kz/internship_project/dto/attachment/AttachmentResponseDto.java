package com.kz.internship_project.dto.attachment;

import java.time.LocalDateTime;

public record AttachmentResponseDto(
        Long id,
        String name,
        String url,
        Long lessonId,
        LocalDateTime createdTime
) {
}
