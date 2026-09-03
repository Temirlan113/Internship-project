package com.kz.internship_project.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequestDto(
        @NotBlank(message = "Refresh token не может быть пустым")
        String refreshToken
) {
}
