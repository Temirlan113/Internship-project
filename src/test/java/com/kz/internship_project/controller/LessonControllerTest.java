package com.kz.internship_project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kz.internship_project.config.JacksonConfig;
import com.kz.internship_project.dto.lesson.LessonCreateDto;
import com.kz.internship_project.dto.lesson.LessonResponseDto;
import com.kz.internship_project.exception.GlobalExceptionHandler;
import com.kz.internship_project.service.LessonService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = LessonController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class, OAuth2ResourceServerAutoConfiguration.class})
@Import({GlobalExceptionHandler.class, JacksonConfig.class})
class LessonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LessonService lessonService;

    @Autowired
    private ObjectMapper objectMapper;

    private LessonCreateDto validCreateDto;
    private LessonResponseDto validResponseDto;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        validCreateDto = new LessonCreateDto("Строки", "Урок про строки", "Текст урока...", 1L);
        validResponseDto = new LessonResponseDto(1L, "Строки", "Урок про строки", "Текст урока...", 1, 1L, now, now);
    }

    //----------------------------------
    // Позитивные сценарии
    //----------------------------------

    @Test
    void create_Success_Returns201() throws Exception {
        // Arrange
        Mockito.when(lessonService.create(Mockito.any(LessonCreateDto.class))).thenReturn(validResponseDto);

        // Act & Assert
        mockMvc.perform(post("/api/v1/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(validResponseDto.id()))
                .andExpect(jsonPath("$.name").value(validResponseDto.name()))
                .andExpect(jsonPath("$.description").value(validResponseDto.description()))
                .andExpect(jsonPath("$.content").value(validResponseDto.content()))
                .andExpect(jsonPath("$.lessonOrder").value(validResponseDto.lessonOrder()))
                .andExpect(jsonPath("$.chapterId").value(validResponseDto.chapterId()));

        Mockito.verify(lessonService, Mockito.times(1)).create(Mockito.any(LessonCreateDto.class));
    }

    @Test
    void getById_Success_Returns200() throws Exception {
        // Arrange
        Long lessonId = 1L;
        Mockito.when(lessonService.getById(lessonId)).thenReturn(validResponseDto);

        // Act & Assert
        mockMvc.perform(get("/api/v1/lessons/{id}", lessonId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(validResponseDto.id()))
                .andExpect(jsonPath("$.name").value(validResponseDto.name()));

        Mockito.verify(lessonService, Mockito.times(1)).getById(lessonId);
    }

    @Test
    void update_Success_Returns200() throws Exception {
        // Arrange
        Long lessonId = 1L;
        LessonCreateDto updateDto = new LessonCreateDto("Числа", "Урок про числа", "Новый текст...", 1L);
        LessonResponseDto updatedResponseDto = new LessonResponseDto(lessonId, "Числа", "Урок про числа", "Новый текст...", 1, 1L, LocalDateTime.now(), LocalDateTime.now());

        Mockito.when(lessonService.update(Mockito.eq(lessonId), Mockito.any(LessonCreateDto.class))).thenReturn(updatedResponseDto);

        // Act & Assert
        mockMvc.perform(put("/api/v1/lessons/{id}", lessonId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(lessonId))
                .andExpect(jsonPath("$.name").value("Числа"))
                .andExpect(jsonPath("$.description").value("Урок про числа"))
                .andExpect(jsonPath("$.content").value("Новый текст..."));

        Mockito.verify(lessonService, Mockito.times(1)).update(Mockito.eq(lessonId), Mockito.any(LessonCreateDto.class));
    }

    @Test
    void delete_Success_Returns204() throws Exception {
        // Arrange
        Long lessonId = 1L;
        Mockito.doNothing().when(lessonService).delete(lessonId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/lessons/{id}", lessonId))
                .andExpect(status().isNoContent());

        Mockito.verify(lessonService, Mockito.times(1)).delete(lessonId);
    }

    @Test
    void getByChapterId_Success_Returns200() throws Exception {
        // Arrange
        Long chapterId = 1L;
        LessonResponseDto lesson2 = new LessonResponseDto(2L, "Урок 2", "Описание 2", "Контент 2", 2, chapterId, LocalDateTime.now(), LocalDateTime.now());

        Mockito.when(lessonService.getByChapterId(chapterId)).thenReturn(List.of(validResponseDto, lesson2));

        // Act & Assert
        mockMvc.perform(get("/api/v1/lessons")
                        .param("chapterId", String.valueOf(chapterId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(validResponseDto.id()))
                .andExpect(jsonPath("$[0].name").value(validResponseDto.name()))
                .andExpect(jsonPath("$[1].id").value(lesson2.id()))
                .andExpect(jsonPath("$[1].name").value(lesson2.name()));

        Mockito.verify(lessonService, Mockito.times(1)).getByChapterId(chapterId);
    }

    //----------------------------------
    // Негативные сценарии
    //----------------------------------

    @Test
    void create_InvalidDto_Returns400BadRequest() throws Exception {
        // Arrange
        LessonCreateDto invalidDto = new LessonCreateDto("", "   ", null, -1L);

        // Act & Assert
        mockMvc.perform(post("/api/v1/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        Mockito.verify(lessonService, Mockito.never()).create(Mockito.any());
    }

    @Test
    void create_ChapterNotFound_Returns404NotFound() throws Exception {
        // Arrange
        Mockito.when(lessonService.create(Mockito.any(LessonCreateDto.class)))
                .thenThrow(new EntityNotFoundException("Глава не найдена"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateDto)))
                .andExpect(status().isNotFound());

        Mockito.verify(lessonService, Mockito.times(1)).create(Mockito.any(LessonCreateDto.class));
    }

    @Test
    void getById_NotFound_Returns404NotFound() throws Exception {
        // Arrange
        Long nonExistentId = 99L;
        Mockito.when(lessonService.getById(nonExistentId))
                .thenThrow(new EntityNotFoundException("Урок не найден"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/lessons/{id}", nonExistentId))
                .andExpect(status().isNotFound());

        Mockito.verify(lessonService, Mockito.times(1)).getById(nonExistentId);
    }

    @Test
    void update_InvalidDto_Returns400BadRequest() throws Exception {
        // Arrange
        LessonCreateDto invalidDto = new LessonCreateDto(null, null, null, null);

        // Act & Assert
        mockMvc.perform(put("/api/v1/lessons/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        Mockito.verify(lessonService, Mockito.never()).update(Mockito.anyLong(), Mockito.any());
    }

    @Test
    void update_NotFound_Returns404NotFound() throws Exception {
        // Arrange
        Long nonExistentId = 99L;
        Mockito.when(lessonService.update(Mockito.eq(nonExistentId), Mockito.any(LessonCreateDto.class)))
                .thenThrow(new EntityNotFoundException("Урок не найден"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/lessons/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateDto)))
                .andExpect(status().isNotFound());

        Mockito.verify(lessonService, Mockito.times(1)).update(Mockito.eq(nonExistentId), Mockito.any(LessonCreateDto.class));
    }

    @Test
    void delete_NotFound_Returns404NotFound() throws Exception {
        // Arrange
        Long nonExistentId = 99L;
        Mockito.doThrow(new EntityNotFoundException("Урок не найден"))
                .when(lessonService).delete(nonExistentId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/lessons/{id}", nonExistentId))
                .andExpect(status().isNotFound());

        Mockito.verify(lessonService, Mockito.times(1)).delete(nonExistentId);
    }

    @Test
    void getByChapterId_ChapterNotFound_Returns404NotFound() throws Exception {
        // Arrange
        Long nonExistentChapterId = 99L;
        Mockito.when(lessonService.getByChapterId(nonExistentChapterId))
                .thenThrow(new EntityNotFoundException("Глава не найдена"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/lessons")
                        .param("chapterId", String.valueOf(nonExistentChapterId)))
                .andExpect(status().isNotFound());

        Mockito.verify(lessonService, Mockito.times(1)).getByChapterId(nonExistentChapterId);
    }
}