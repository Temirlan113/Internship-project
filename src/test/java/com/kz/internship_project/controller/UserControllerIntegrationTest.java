package com.kz.internship_project.controller;

import com.kz.internship_project.BaseIntegrationTest;
import com.kz.internship_project.config.KeycloakRoleConverter;
import com.kz.internship_project.dto.user.ChangeUserRoleDto;
import com.kz.internship_project.dto.user.UserCreateDto;
import com.kz.internship_project.dto.user.UserResponseDto;
import com.kz.internship_project.dto.user.UserUpdateDto;
import com.kz.internship_project.enums.RoleUser;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserControllerIntegrationTest extends BaseIntegrationTest {

    @MockitoBean
    private KeycloakService keycloakService;

    private RequestPostProcessor jwtRole(String role) {
        String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        String roleWithoutPrefix = role.replace("ROLE_", "");
        return jwt()
                .authorities(new KeycloakRoleConverter())
                .jwt(jwt -> jwt
                        .subject("test-user-uuid-123")
                        .claim("realm_access", Map.of("roles", List.of(roleWithPrefix, roleWithoutPrefix))));
    }

    // ==========================================
    // 1. POST /api/v1/users/create
    // ==========================================

    @Test
    @DisplayName("POST /users/create - Успешно создает пользователя, если пользователь ROLE_ADMIN")
    void createUser_ShouldReturn201_WhenUserIsAdmin() throws Exception {
        UserCreateDto createDto = new UserCreateDto(
                "newuser",
                "user@test.com",
                "password123",
                "John",
                "Doe",
                RoleUser.ROLE_STUDENT
        );

        UserResponseDto responseDto = new UserResponseDto(
                "uuid-999",
                "newuser",
                "user@test.com",
                RoleUser.ROLE_STUDENT,
                true,
                false
        );

        when(keycloakService.createUser(any(UserCreateDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto))
                        .with(jwtRole(RoleUser.ROLE_ADMIN.name())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("uuid-999")))
                .andExpect(jsonPath("$.username", is("newuser")))
                .andExpect(jsonPath("$.email", is("user@test.com")))
                .andExpect(jsonPath("$.role", is("ROLE_STUDENT")));
    }

    @Test
    @DisplayName("POST /users/create - Возвращает 403 Forbidden, если пользователь ROLE_STUDENT")
    void createUser_ShouldReturn403_WhenUserIsStudent() throws Exception {
        UserCreateDto createDto = new UserCreateDto(
                "newuser",
                "user@test.com",
                "password123",
                "John",
                "Doe",
                RoleUser.ROLE_STUDENT
        );

        mockMvc.perform(post("/api/v1/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto))
                        .with(jwtRole(RoleUser.ROLE_STUDENT.name())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /users/create - Возвращает 400 Bad Request при невалидном DTO (пустой email)")
    void createUser_ShouldReturn400_WhenDtoIsInvalid() throws Exception {
        UserCreateDto invalidDto = new UserCreateDto(
                "newuser",
                "not-an-email", // невалидный email
                "pass",          // слишком короткий пароль
                "John",
                "Doe",
                null             // пустая роль
        );

        mockMvc.perform(post("/api/v1/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .with(jwtRole(RoleUser.ROLE_ADMIN.name())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    // ==========================================
    // 2. PUT /api/v1/users/update-profile
    // ==========================================

    @Test
    @DisplayName("PUT /users/update-profile - Успешно обновляет профиль аутентифицированного пользователя")
    void updateMyProfile_ShouldReturn204_WhenUserIsAuthenticated() throws Exception {
        UserUpdateDto updateDto = new UserUpdateDto(
                "new@test.com",
                "oldPassword",
                "newPassword",
                "Alex",
                "Smith"
        );

        doNothing().when(keycloakService).updateUser(eq("test-user-uuid-123"), any(UserUpdateDto.class));

        mockMvc.perform(put("/api/v1/users/update-profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .with(jwtRole(RoleUser.ROLE_STUDENT.name())))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PUT /users/update-profile - Возвращает 401 Unauthorized, если пользователь не аутентифицирован")
    void updateMyProfile_ShouldReturn401_WhenUnauthenticated() throws Exception {
        UserUpdateDto updateDto = new UserUpdateDto(
                "new@test.com",
                "oldPassword",
                "newPassword",
                "Alex",
                "Smith"
        );

        mockMvc.perform(put("/api/v1/users/update-profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isUnauthorized());
    }

    // ==========================================
    // 3. PUT /api/v1/users/{userId}/role
    // ==========================================

    @Test
    @DisplayName("PUT /users/{userId}/role - Успешно меняет роль пользователя, если вызывающий ROLE_ADMIN")
    void changeUserRole_ShouldReturn204_WhenUserIsAdmin() throws Exception {
        ChangeUserRoleDto roleDto = new ChangeUserRoleDto(RoleUser.ROLE_TEACHER);

        doNothing().when(keycloakService).changeUserRole("target-user-id", RoleUser.ROLE_TEACHER);

        mockMvc.perform(put("/api/v1/users/{userId}/role", "target-user-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleDto))
                        .with(jwtRole(RoleUser.ROLE_ADMIN.name())))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PUT /users/{userId}/role - Возвращает 403 Forbidden, если роль меняет не администратор")
    void changeUserRole_ShouldReturn403_WhenUserIsNotAdmin() throws Exception {
        ChangeUserRoleDto roleDto = new ChangeUserRoleDto(RoleUser.ROLE_TEACHER);

        mockMvc.perform(put("/api/v1/users/{userId}/role", "target-user-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleDto))
                        .with(jwtRole(RoleUser.ROLE_TEACHER.name())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /users/{userId}/role - Возвращает 400 Bad Request, если передана пустая роль")
    void changeUserRole_ShouldReturn400_WhenRoleIsNull() throws Exception {
        String invalidJson = "{\"role\": null}";

        mockMvc.perform(put("/api/v1/users/{userId}/role", "target-user-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson)
                        .with(jwtRole(RoleUser.ROLE_ADMIN.name())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }
}