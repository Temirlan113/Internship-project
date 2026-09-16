package com.kz.internship_project.dto.attachment;

import org.springframework.core.io.InputStreamResource;

public record AttachmentDownloadDto(
        InputStreamResource resource,
        String fileName
) {
}
