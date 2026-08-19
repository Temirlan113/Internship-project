package com.kz.internship_project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kz.internship_project.dto.LessonCreateDto;
import com.kz.internship_project.dto.LessonResponseDto;
import com.kz.internship_project.service.LessonService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LessonController.class)
class LessonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LessonService lessonService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void create_Success_Returns201() throws Exception {
        // Arrange
        LessonCreateDto createDto = new LessonCreateDto("Строки", "Урок про строки", "Текст урока...", 1L);
        LessonResponseDto responseDto = new LessonResponseDto(1L, "Строки", "Урок про строки", "Текст урока...", 1, 1L,LocalDateTime.now(),LocalDateTime.now());

        Mockito.when(lessonService.create(Mockito.any(LessonCreateDto.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/v1/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Строки"))
                .andExpect(jsonPath("$.description").value("Урок про строки"))
                .andExpect(jsonPath("$.content").value("Текст урока..."))
                .andExpect(jsonPath("$.lessonOrder").value(1))
                .andExpect(jsonPath("$.chapterId").value(1L));

        Mockito.verify(lessonService, Mockito.times(1)).create(Mockito.any(LessonCreateDto.class));
    }

    @Test
    void getById_Success_Returns200() throws Exception {
        // Arrange
        Long lessonId = 1L;
        LessonResponseDto responseDto = new LessonResponseDto(lessonId, "Строки", "Урок про строки", "Текст урока...", 1, 1L,LocalDateTime.now(),LocalDateTime.now());

        Mockito.when(lessonService.getById(lessonId)).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(get("/api/v1/lessons/{id}", lessonId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Строки"));

        Mockito.verify(lessonService, Mockito.times(1)).getById(lessonId);
    }

    @Test
    void update_Success_Returns200() throws Exception {
        // Arrange
        Long lessonId = 1L;
        LessonCreateDto updateDto = new LessonCreateDto("Числа", "Урок про числа", "Новый текст...", 1L);
        LessonResponseDto responseDto = new LessonResponseDto(lessonId, "Числа", "Урок про числа", "Новый текст...", 1, 1L, LocalDateTime.now(),LocalDateTime.now());

        Mockito.when(lessonService.update(Mockito.eq(lessonId), Mockito.any(LessonCreateDto.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(put("/api/v1/lessons/{id}", lessonId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
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
        LessonResponseDto lesson1 = new LessonResponseDto(1L, "Урок 1", "Описание 1", "Контент 1", 1, chapterId, LocalDateTime.now(), LocalDateTime.now());
        LessonResponseDto lesson2 = new LessonResponseDto(2L, "Урок 2", "Описание 2", "Контент 2", 2, chapterId, LocalDateTime.now(),LocalDateTime.now());

        Mockito.when(lessonService.getByChapterId(chapterId)).thenReturn(List.of(lesson1, lesson2));

        // Act & Assert
        mockMvc.perform(get("/api/v1/lessons")
                        .param("chapterId", String.valueOf(chapterId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Урок 1"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Урок 2"));

        Mockito.verify(lessonService, Mockito.times(1)).getByChapterId(chapterId);
    }
}