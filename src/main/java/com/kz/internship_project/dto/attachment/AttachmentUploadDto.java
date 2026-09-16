package com.kz.internship_project.dto.attachment;

import org.springframework.web.multipart.MultipartFile;

public record AttachmentUploadDto(
        MultipartFile file,
        Long lessonId
) {
}
