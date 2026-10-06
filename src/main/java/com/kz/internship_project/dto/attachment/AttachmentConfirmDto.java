package com.kz.internship_project.dto.attachment;

public record AttachmentConfirmDto(

        String name,
        String fileKey,
        Long lessonId
) {
}
