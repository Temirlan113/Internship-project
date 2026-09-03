package com.kz.internship_project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kz.internship_project.config.JacksonConfig;
import com.kz.internship_project.dto.auth.AuthResponseDto;
import com.kz.internship_project.dto.auth.JwtResponseDto;
import com.kz.internship_project.dto.auth.LoginCreateDto;
import com.kz.internship_project.dto.auth.RefreshTokenRequestDto;
import com.kz.internship_project.exception.GlobalExceptionHandler;
import com.kz.internship_project.service.impl.KeyCloakServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = AuthController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class, OAuth2ResourceServerAutoConfiguration.class})
@Import({GlobalExceptionHandler.class, JacksonConfig.class})

class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private KeyCloakServiceImpl keyCloakService;

    @Autowired
    private ObjectMapper objectMapper;

    private LoginCreateDto validLoginDto;
    private JwtResponseDto validJwtResponseDto;
    private RefreshTokenRequestDto validRefreshTokenDto;
    private AuthResponseDto validAuthResponseDto;

    @BeforeEach
    void setUp() {
        validLoginDto = new LoginCreateDto("user", "password123");
        validJwtResponseDto = new JwtResponseDto("access-token-123", "refresh-token-123", 3600, "Bearer");

        validRefreshTokenDto = new RefreshTokenRequestDto("refresh-token-123");
        validAuthResponseDto = new AuthResponseDto("new-access-token-123", "new-refresh-token-123", 3600L);
    }

    //----------------------------------
    // Позитивные сценарии
    //----------------------------------

    @Test
    void login_Success_Returns200() throws Exception {
        // Arrange
        Mockito.when(keyCloakService.login(Mockito.any(LoginCreateDto.class)))
                .thenReturn(validJwtResponseDto);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value(validJwtResponseDto.accessToken()))
                .andExpect(jsonPath("$.refresh_token").value(validJwtResponseDto.refreshToken()))
                .andExpect(jsonPath("$.expires_in").value(validJwtResponseDto.expiresIn()))
                .andExpect(jsonPath("$.token_type").value(validJwtResponseDto.tokenType()));

        Mockito.verify(keyCloakService, Mockito.times(1)).login(Mockito.any(LoginCreateDto.class));
    }

    @Test
    void refreshToken_Success_Returns200() throws Exception {
        // Arrange
        Mockito.when(keyCloakService.refreshToken(Mockito.any(RefreshTokenRequestDto.class)))
                .thenReturn(validAuthResponseDto);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRefreshTokenDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(validAuthResponseDto.accessToken()))
                .andExpect(jsonPath("$.refreshToken").value(validAuthResponseDto.refreshToken()))
                .andExpect(jsonPath("$.expiresIn").value(validAuthResponseDto.expiresIn()));

        Mockito.verify(keyCloakService, Mockito.times(1)).refreshToken(Mockito.any(RefreshTokenRequestDto.class));
    }

    //----------------------------------
    // Негативные сценарии
    //----------------------------------

    @Test
    void login_InvalidCredentials_Returns500OrHandledException() throws Exception {
        // Arrange
        Mockito.when(keyCloakService.login(Mockito.any(LoginCreateDto.class)))
                .thenThrow(new BadCredentialsException("Неверный логин или пароль"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginDto)))
                .andExpect(status().isUnauthorized());

        Mockito.verify(keyCloakService, Mockito.times(1)).login(Mockito.any(LoginCreateDto.class));
    }

    @Test
    void refreshToken_EmptyToken_Returns400BadRequest() throws Exception {
        // Arrange
        RefreshTokenRequestDto invalidDto = new RefreshTokenRequestDto("");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        Mockito.verify(keyCloakService, Mockito.never()).refreshToken(Mockito.any());
    }

    @Test
    void refreshToken_NullToken_Returns400BadRequest() throws Exception {
        // Arrange
        RefreshTokenRequestDto invalidDto = new RefreshTokenRequestDto(null);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        Mockito.verify(keyCloakService, Mockito.never()).refreshToken(Mockito.any());
    }

    @Test
    void refreshToken_InvalidOrExpiredToken_Returns500OrHandledException() throws Exception {
        // Arrange
        Mockito.when(keyCloakService.refreshToken(Mockito.any(RefreshTokenRequestDto.class)))
                .thenThrow(new BadCredentialsException("Невалидный или просроченный Refresh Token"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRefreshTokenDto)))
                .andExpect(status().isUnauthorized());

        Mockito.verify(keyCloakService, Mockito.times(1)).refreshToken(Mockito.any(RefreshTokenRequestDto.class));
    }
}