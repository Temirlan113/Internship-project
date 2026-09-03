package com.kz.internship_project.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record JwtResponseDto(@JsonProperty("access_token") String accessToken,
                             @JsonProperty("refresh_token") String refreshToken,
                             @JsonProperty("expires_in") long expiresIn,
                             @JsonProperty("token_type") String tokenType) {
}
