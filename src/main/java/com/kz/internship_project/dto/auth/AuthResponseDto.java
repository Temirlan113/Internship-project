package com.kz.internship_project.dto.auth;

public record AuthResponseDto(
        String accessToken,
        String refreshToken,
        Long expiresIn
) {
}
