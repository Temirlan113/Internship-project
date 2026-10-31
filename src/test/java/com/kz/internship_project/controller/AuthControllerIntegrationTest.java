package com.kz.internship_project.controller;

import com.kz.internship_project.BaseIntegrationTest;
import com.kz.internship_project.config.KeycloakRoleConverter;
import com.kz.internship_project.dto.auth.AuthResponseDto;
import com.kz.internship_project.dto.auth.JwtResponseDto;
import com.kz.internship_project.dto.auth.LoginCreateDto;
import com.kz.internship_project.dto.auth.RefreshTokenRequestDto;
import com.kz.internship_project.service.KeycloakService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @MockitoBean
    private KeycloakService keycloakService;

    // ==========================================
    // 1. POST /api/v1/auth/login
    // ==========================================

    @Test
    @DisplayName("POST /login - Успешная авторизация, возвращает JWT токены (permitAll)")
    void login_ShouldReturn200_WhenCredentialsAreValid() throws Exception {
        LoginCreateDto loginDto = new LoginCreateDto("admin", "password123");
        JwtResponseDto jwtResponse = new JwtResponseDto(
                "mock-access-token",
                "mock-refresh-token",
                3600L,
                "Bearer"
        );

        when(keycloakService.login(any(LoginCreateDto.class))).thenReturn(jwtResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token", is("mock-access-token")))
                .andExpect(jsonPath("$.refresh_token", is("mock-refresh-token")))
                .andExpect(jsonPath("$.expires_in", is(3600)))
                .andExpect(jsonPath("$.token_type", is("Bearer")));
    }

    @Test
    @DisplayName("POST /login - Эндпоинт доступен без токена (permitAll)")
    void login_ShouldBeAccessibleWithoutToken() throws Exception {
        LoginCreateDto loginDto = new LoginCreateDto("user", "password");
        when(keycloakService.login(any(LoginCreateDto.class))).thenReturn(new JwtResponseDto("acc", "ref", 100L, "Bearer"));

        // Выполняем запрос БЕЗ .with(jwt())
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk());
    }

    // ==========================================
    // 2. POST /api/v1/auth/refresh-token
    // ==========================================

    @Test
    @DisplayName("POST /refresh-token - Успешное обновление токена")
    void refreshToken_ShouldReturn200_WhenTokenIsValid() throws Exception {
        RefreshTokenRequestDto requestDto = new RefreshTokenRequestDto("valid-refresh-token");
        AuthResponseDto authResponse = new AuthResponseDto(
                "new-access-token",
                "new-refresh-token",
                3600L
        );

        when(keycloakService.refreshToken(any(RefreshTokenRequestDto.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", is("new-access-token")))
                .andExpect(jsonPath("$.refreshToken", is("new-refresh-token")))
                .andExpect(jsonPath("$.expiresIn", is(3600)));
    }

    @Test
    @DisplayName("POST /refresh-token - Возвращает 400 Bad Request, если refresh token пустой (Validation)")
    void refreshToken_ShouldReturn400_WhenTokenIsBlank() throws Exception {
        RefreshTokenRequestDto invalidDto = new RefreshTokenRequestDto("");

        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    @DisplayName("POST /refresh-token - Эндпоинт доступен публично (permitAll)")
    void refreshToken_ShouldBeAccessibleWithoutToken() throws Exception {
        RefreshTokenRequestDto requestDto = new RefreshTokenRequestDto("some-token");
        when(keycloakService.refreshToken(any(RefreshTokenRequestDto.class)))
                .thenReturn(new AuthResponseDto("acc", "ref", 100L));

        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }
}