package com.kz.internship_project.controller;

import com.kz.internship_project.BaseIntegrationTest;
import com.kz.internship_project.config.KeycloakRoleConverter;
import com.kz.internship_project.dto.attachment.AttachmentConfirmDto;
import com.kz.internship_project.dto.attachment.UploadRequestDto;
import com.kz.internship_project.entity.Attachment;
import com.kz.internship_project.entity.Chapter;
import com.kz.internship_project.entity.Course;
import com.kz.internship_project.entity.Lesson;
import com.kz.internship_project.enums.RoleUser;
import com.kz.internship_project.repository.AttachmentRepository;
import com.kz.internship_project.repository.ChapterRepository;
import com.kz.internship_project.repository.CourseRepository;
import com.kz.internship_project.repository.LessonRepository;
import com.kz.internship_project.service.FileService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AttachmentControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private CourseRepository courseRepository;

    @MockitoBean
    private FileService fileService;

    @BeforeEach
    void setUp() {
        attachmentRepository.deleteAll();
        lessonRepository.deleteAll();
        chapterRepository.deleteAll();
        courseRepository.deleteAll();
    }

    private RequestPostProcessor jwtRole(String role) {
        String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        String roleWithoutPrefix = role.replace("ROLE_", "");
        return jwt()
                .authorities(new KeycloakRoleConverter())
                .jwt(jwt -> jwt
                        .subject("test-user-id")
                        .claim("realm_access", Map.of("roles", List.of(roleWithPrefix, roleWithoutPrefix))));
    }

    // ==========================================
    // 1. POST /api/v1/attachments/presigned-upload-url
    // ==========================================

    @Test
    @DisplayName("POST /presigned-upload-url - Успешно возвращает URL, если пользователь ADMIN")
    void getUploadUrl_ShouldReturn200_WhenUserIsAdmin() throws Exception {
        UploadRequestDto requestDto = new UploadRequestDto("test-file.png");
        when(fileService.generatePresignedUploadUrl(anyString())).thenReturn("https://minio.url/presigned-put");

        mockMvc.perform(post("/api/v1/attachments/presigned-upload-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(jwtRole(RoleUser.ROLE_ADMIN.name())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uploadUrl", is("https://minio.url/presigned-put")))
                .andExpect(jsonPath("$.fileKey").exists());
    }

    @Test
    @DisplayName("POST /presigned-upload-url - Возвращает 403 Forbidden, если пользователь STUDENT")
    void getUploadUrl_ShouldReturn403_WhenUserIsStudent() throws Exception {
        UploadRequestDto requestDto = new UploadRequestDto("test-file.png");

        mockMvc.perform(post("/api/v1/attachments/presigned-upload-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(jwtRole(RoleUser.ROLE_STUDENT.name())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /presigned-upload-url - Возвращает 400 Bad Request при пустом имени файла")
    void getUploadUrl_ShouldReturn400_WhenDtoIsInvalid() throws Exception {
        UploadRequestDto invalidDto = new UploadRequestDto("");

        mockMvc.perform(post("/api/v1/attachments/presigned-upload-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .with(jwtRole(RoleUser.ROLE_ADMIN.name())))
                .andExpect(status().isBadRequest());
    }

    // ==========================================
    // 2. POST /api/v1/attachments/confirm
    // ==========================================

    @Test
    @DisplayName("POST /confirm - Успешно подтверждает загрузку и сохраняет в БД, если пользователь TEACHER")
    void confirmUpload_ShouldReturn201_AndSaveToDatabase_WhenUserIsTeacher() throws Exception {
        // Arrange
        Course course = courseRepository.save(new Course(null, "Course", "Desc", null, null));
        Chapter chapter = chapterRepository.save(new Chapter(null, "Chapter", "Desc", 1, course, null, null));
        Lesson lesson = lessonRepository.save(new Lesson(null, "Lesson", "Desc", "Content", 1, chapter, null, null));

        AttachmentConfirmDto confirmDto = new AttachmentConfirmDto("document.pdf", "key-uuid-123", lesson.getId());

        when(fileService.exists("key-uuid-123")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/v1/attachments/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmDto))
                        .with(jwtRole(RoleUser.ROLE_TEACHER.name())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name", is("document.pdf")))
                .andExpect(jsonPath("$.lessonId", is(lesson.getId().intValue())));

        // DB Assertion
        assertEquals(1, attachmentRepository.count());
        Attachment saved = attachmentRepository.findAll().get(0);
        assertEquals("document.pdf", saved.getName());
        assertEquals("key-uuid-123", saved.getUrl());
        assertEquals(lesson.getId(), saved.getLesson().getId());
    }

    @Test
    @DisplayName("POST /confirm - Возвращает 400 Bad Request, если файл отсутствует в MinIO")
    void confirmUpload_ShouldReturn400_WhenFileMissingInMinio() throws Exception {
        Course course = courseRepository.save(new Course(null, "Course", "Desc", null, null));
        Chapter chapter = chapterRepository.save(new Chapter(null, "Chapter", "Desc", 1, course, null, null));
        Lesson lesson = lessonRepository.save(new Lesson(null, "Lesson", "Desc", "Content", 1, chapter, null, null));

        AttachmentConfirmDto confirmDto = new AttachmentConfirmDto("document.pdf", "missing-key", lesson.getId());

        when(fileService.exists("missing-key")).thenReturn(false);

        mockMvc.perform(post("/api/v1/attachments/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmDto))
                        .with(jwtRole(RoleUser.ROLE_ADMIN.name())))
                .andExpect(status().isBadRequest());

        assertEquals(0, attachmentRepository.count());
    }

    @Test
    @DisplayName("POST /confirm - Возвращает 404 Not Found, если урок не существует")
    void confirmUpload_ShouldReturn404_WhenLessonNotFound() throws Exception {
        AttachmentConfirmDto confirmDto = new AttachmentConfirmDto("document.pdf", "key-uuid", 999L);
        when(fileService.exists("key-uuid")).thenReturn(true);

        mockMvc.perform(post("/api/v1/attachments/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmDto))
                        .with(jwtRole(RoleUser.ROLE_ADMIN.name())))
                .andExpect(status().isNotFound());
    }

    // ==========================================
    // 3. GET /api/v1/attachments/download/{attachmentId}
    // ==========================================

    @Test
    @DisplayName("GET /download/{id} - Успешно отдает файл, если пользователь STUDENT")
    void download_ShouldReturn200_WhenUserIsStudent() throws Exception {
        Course course = courseRepository.save(new Course(null, "Course", "Desc", null, null));
        Chapter chapter = chapterRepository.save(new Chapter(null, "Chapter", "Desc", 1, course, null, null));
        Lesson lesson = lessonRepository.save(new Lesson(null, "Lesson", "Desc", "Content", 1, chapter, null, null));

        Attachment attachment = attachmentRepository.save(
                new Attachment(null, "file.txt", "url-key", lesson, null)
        );

        ByteArrayInputStream inputStream = new ByteArrayInputStream("file content".getBytes());
        InputStreamResource resource = new InputStreamResource(inputStream);

        when(fileService.downloadFile("url-key")).thenReturn(resource);

        mockMvc.perform(get("/api/v1/attachments/download/{attachmentId}", attachment.getId())
                        .with(jwtRole(RoleUser.ROLE_STUDENT.name())))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(header().string("Content-Disposition", Matchers.containsString("file.txt")));
    }

    @Test
    @DisplayName("GET /download/{id} - Возвращает 403 Forbidden для ROLE_USER (не купивший курс)")
    void download_ShouldReturn403_WhenUserIsPlainUser() throws Exception {
        mockMvc.perform(get("/api/v1/attachments/download/{attachmentId}", 1L)
                        .with(jwtRole(RoleUser.ROLE_USER.name())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /download/{id} - Возвращает 404 Not Found, если вложение не существует")
    void download_ShouldReturn404_WhenAttachmentNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/attachments/download/{attachmentId}", 999L)
                        .with(jwtRole(RoleUser.ROLE_STUDENT.name())))
                .andExpect(status().isNotFound());
    }

    // ==========================================
    // 4. DELETE /api/v1/attachments/{attachmentId}
    // ==========================================

    @Test
    @DisplayName("DELETE /{id} - Успешно удаляет файл из MinIO и запись из БД, если пользователь ADMIN")
    void delete_ShouldReturn204_AndRemoveFromDatabase_WhenUserIsAdmin() throws Exception {
        Course course = courseRepository.save(new Course(null, "Course", "Desc", null, null));
        Chapter chapter = chapterRepository.save(new Chapter(null, "Chapter", "Desc", 1, course, null, null));
        Lesson lesson = lessonRepository.save(new Lesson(null, "Lesson", "Desc", "Content", 1, chapter, null, null));

        Attachment attachment = attachmentRepository.save(
                new Attachment(null, "file.txt", "url-key-del", lesson, null)
        );

        doNothing().when(fileService).deleteFile("url-key-del");

        mockMvc.perform(delete("/api/v1/attachments/{attachmentId}", attachment.getId())
                        .with(jwtRole(RoleUser.ROLE_ADMIN.name())))
                .andExpect(status().isNoContent());

        // DB Assertion
        assertEquals(0, attachmentRepository.count());
        assertFalse(attachmentRepository.existsById(attachment.getId()));
        verify(fileService, times(1)).deleteFile("url-key-del");
    }

    @Test
    @DisplayName("DELETE /{id} - Возвращает 403 Forbidden, если пользователь STUDENT")
    void delete_ShouldReturn403_WhenUserIsStudent() throws Exception {
        mockMvc.perform(delete("/api/v1/attachments/{attachmentId}", 1L)
                        .with(jwtRole(RoleUser.ROLE_STUDENT.name())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /{id} - Возвращает 404 Not Found, если вложение не существует")
    void delete_ShouldReturn404_WhenAttachmentNotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/attachments/{attachmentId}", 999L)
                        .with(jwtRole(RoleUser.ROLE_ADMIN.name())))
                .andExpect(status().isNotFound());
    }
}