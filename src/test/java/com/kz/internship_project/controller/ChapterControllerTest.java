package com.kz.internship_project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kz.internship_project.dto.ChapterCreateDto;
import com.kz.internship_project.dto.ChapterResponseDto;
import com.kz.internship_project.service.ChapterService;
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

@WebMvcTest(ChapterController.class)
class ChapterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChapterService chapterService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void create_Success_Returns201() throws Exception {
        // Arrange
        ChapterCreateDto createDto = new ChapterCreateDto("Переменные", "Строки, числовые, логические переменные", 1L);
        ChapterResponseDto responseDto = new ChapterResponseDto(1L, "Переменные", "Строки, числовые, логические переменные", 1, 1L,LocalDateTime.now(),LocalDateTime.now());

        Mockito.when(chapterService.create(Mockito.any(ChapterCreateDto.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/v1/chapters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Переменные"))
                .andExpect(jsonPath("$.chapterOrder").value(1))
                .andExpect(jsonPath("$.courseId").value(1L));

        Mockito.verify(chapterService, Mockito.times(1)).create(Mockito.any(ChapterCreateDto.class));
    }

    @Test
    void getById_Success_Returns200() throws Exception {
        // Arrange
        Long chapterId = 1L;
        ChapterResponseDto responseDto = new ChapterResponseDto(chapterId, "Переменные", "Строки, числовые, логические переменные", 1, 1L,LocalDateTime.now(),LocalDateTime.now());

        Mockito.when(chapterService.getById(chapterId)).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(get("/api/v1/chapters/{id}", chapterId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Переменные"));

        Mockito.verify(chapterService, Mockito.times(1)).getById(chapterId);
    }

    @Test
    void update_Success_Returns200() throws Exception {
        // Arrange
        Long chapterId = 1L;
        ChapterCreateDto updateDto = new ChapterCreateDto("Generics", "Как работают дженерики", 1L);
        ChapterResponseDto responseDto = new ChapterResponseDto(chapterId, "Generics", "Как работают дженерики", 1, 1L, LocalDateTime.now(),LocalDateTime.now());

        Mockito.when(chapterService.update(Mockito.eq(chapterId), Mockito.any(ChapterCreateDto.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(put("/api/v1/chapters/{id}", chapterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Generics"))
                .andExpect(jsonPath("$.description").value("Как работают дженерики"));

        Mockito.verify(chapterService, Mockito.times(1)).update(Mockito.eq(chapterId), Mockito.any(ChapterCreateDto.class));
    }

    @Test
    void delete_Success_Returns204() throws Exception {
        // Arrange
        Long chapterId = 1L;
        Mockito.doNothing().when(chapterService).delete(chapterId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/chapters/{id}", chapterId))
                .andExpect(status().isNoContent());

        Mockito.verify(chapterService, Mockito.times(1)).delete(chapterId);
    }

    @Test
    void getByCourseId_Success_Returns200() throws Exception {
        // Arrange
        Long courseId = 1L;
        ChapterResponseDto chapter1 = new ChapterResponseDto(1L, "Переменные", "Описание 1", 1, courseId, LocalDateTime.now(), LocalDateTime.now());
        ChapterResponseDto chapter2 = new ChapterResponseDto(2L, "Циклы", "Описание 2", 2, courseId, LocalDateTime.now(),LocalDateTime.now());

        Mockito.when(chapterService.getByCourseId(courseId)).thenReturn(List.of(chapter1, chapter2));

        // Act & Assert
        mockMvc.perform(get("/api/v1/chapters")
                        .param("courseId", String.valueOf(courseId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Переменные"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Циклы"));

        Mockito.verify(chapterService, Mockito.times(1)).getByCourseId(courseId);
    }
}