package com.kz.internship_project.dto.attachment;

public record PresignedUrlResponseDto(
        String uploadUrl,
        String fileKey
) {
}
