package com.kz.internship_project.controller;

import com.kz.internship_project.BaseIntegrationTest;
import com.kz.internship_project.config.KeycloakRoleConverter;
import com.kz.internship_project.dto.attachment.*;
import com.kz.internship_project.enums.RoleUser;
import com.kz.internship_project.service.AttachmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AttachmentControllerIntegrationTest extends BaseIntegrationTest {

    @MockitoBean
    private AttachmentService attachmentService;

    private RequestPostProcessor jwtAdmin() {
        return jwt()
                .authorities(new KeycloakRoleConverter())
                .jwt(jwt -> jwt
                        .subject("test-admin-id")
                        .claim("realm_access", Map.of("roles", List.of(RoleUser.ROLE_ADMIN.name()))));
    }

    private RequestPostProcessor jwtStudent() {
        return jwt()
                .authorities(new KeycloakRoleConverter())
                .jwt(jwt -> jwt
                        .subject("test-admin-id")
                        .claim("realm_access", Map.of("roles", List.of(RoleUser.ROLE_STUDENT.name()))));
    }

    private RequestPostProcessor jwtUser() {
        return jwt()
                .authorities(new KeycloakRoleConverter())
                .jwt(jwt -> jwt
                        .subject("test-admin-id")
                        .claim("realm_access", Map.of("roles", List.of(RoleUser.ROLE_USER.name()))));
    }

    private RequestPostProcessor jwtTeacher() {
        return jwt()
                .authorities(new KeycloakRoleConverter())
                .jwt(jwt -> jwt
                        .subject("test-admin-id")
                        .claim("realm_access", Map.of("roles", List.of(RoleUser.ROLE_TEACHER.name()))));
    }

    // ==========================================
    // 1. POST /api/v1/attachments/presigned-upload-url
    // ==========================================

    @Test
    @DisplayName("POST /presigned-upload-url - Успешно возвращает URL для загрузки, если пользователь ADMIN")
    void getUploadUrl_ShouldReturn200_WhenUserIsAdmin() throws Exception {
        UploadRequestDto requestDto = new UploadRequestDto("test-file.png");
        PresignedUrlResponseDto responseDto = new PresignedUrlResponseDto("https://minio.url/upload", "file-key-123");

        when(attachmentService.getPresignedUploadUrl(any(UploadRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/attachments/presigned-upload-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(jwtAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uploadUrl", is("https://minio.url/upload")))
                .andExpect(jsonPath("$.fileKey", is("file-key-123")));
    }

    @Test
    @DisplayName("POST /presigned-upload-url - Возвращает 403 Forbidden, если пользователь STUDENT")
    void getUploadUrl_ShouldReturn403_WhenUserIsStudent() throws Exception {
        UploadRequestDto requestDto = new UploadRequestDto("test-file.png");

        mockMvc.perform(post("/api/v1/attachments/presigned-upload-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(jwtStudent()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /presigned-upload-url - Возвращает 400 Bad Request при невалидном DTO")
    void getUploadUrl_ShouldReturn400_WhenDtoIsInvalid() throws Exception {
        UploadRequestDto invalidDto = new UploadRequestDto(""); // пустое имя файла

        mockMvc.perform(post("/api/v1/attachments/presigned-upload-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .with(jwtAdmin()))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("POST /confirm - Успешно подтверждает загрузку, если пользователь TEACHER")
    void confirmUpload_ShouldReturn201_WhenUserIsTeacher() throws Exception {
        AttachmentConfirmDto confirmDto = new AttachmentConfirmDto("file.png", "key-123", 1L);
        AttachmentResponseDto responseDto = new AttachmentResponseDto(
                10L, "file.png", "https://minio.url/file.png", 1L, LocalDateTime.now()
        );

        when(attachmentService.confirmUpload(any(AttachmentConfirmDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/attachments/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmDto))
                        .with(jwtTeacher()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.name", is("file.png")))
                .andExpect(jsonPath("$.lessonId", is(1)));
    }

    @Test
    @DisplayName("POST /confirm - Возвращает 403 Forbidden, если пользователь STUDENT")
    void confirmUpload_ShouldReturn403_WhenUserIsStudent() throws Exception {
        AttachmentConfirmDto confirmDto = new AttachmentConfirmDto("file.png", "key-123", 1L);

        mockMvc.perform(post("/api/v1/attachments/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmDto))
                        .with(jwtStudent()))
                .andExpect(status().isForbidden());
    }


    @Test
    @DisplayName("GET /download/{id} - Успешно отдает файл, если пользователь STUDENT")
    void download_ShouldReturn200_WhenUserIsStudent() throws Exception {
        ByteArrayResource resource = new ByteArrayResource("test file content".getBytes());
        InputStreamResource inputStreamResource = new InputStreamResource(resource.getInputStream());
        AttachmentDownloadDto downloadDto = new AttachmentDownloadDto(inputStreamResource, "document.pdf");

        when(attachmentService.downloadAttachment(1L)).thenReturn(downloadDto);

        mockMvc.perform(get("/api/v1/attachments/download/{attachmentId}", 1L)
                        .with(jwtStudent()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(header().string("Content-Disposition", "attachment; filename*=utf-8''document.pdf"));
    }

    @Test
    @DisplayName("GET /download/{id} - Возвращает 403 Forbidden, если пользователь ROLE_USER (не купивший курс)")
    void download_ShouldReturn403_WhenUserIsPlainUser() throws Exception {
        mockMvc.perform(get("/api/v1/attachments/download/{attachmentId}", 1L)
                        .with(jwtUser()))
                .andExpect(status().isForbidden());
    }


    @Test
    @DisplayName("DELETE /{id} - Успешно удаляет вложение, если пользователь ADMIN")
    void delete_ShouldReturn204_WhenUserIsAdmin() throws Exception {
        doNothing().when(attachmentService).deleteAttachment(1L);

        mockMvc.perform(delete("/api/v1/attachments/{attachmentId}", 1L)
                        .with(jwtAdmin()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /{id} - Возвращает 403 Forbidden, если пользователь STUDENT")
    void delete_ShouldReturn403_WhenUserIsStudent() throws Exception {
        mockMvc.perform(delete("/api/v1/attachments/{attachmentId}", 1L)
                        .with(jwtStudent()))
                .andExpect(status().isForbidden());
    }
}