package com.kz.internship_project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kz.internship_project.config.JacksonConfig;
import com.kz.internship_project.dto.chapter.ChapterCreateDto;
import com.kz.internship_project.dto.chapter.ChapterResponseDto;
import com.kz.internship_project.exception.GlobalExceptionHandler;
import com.kz.internship_project.service.ChapterService;
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

@WebMvcTest(
        value = ChapterController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class
        }
)
@Import({GlobalExceptionHandler.class, JacksonConfig.class})
class ChapterControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChapterService chapterService;


    private ChapterCreateDto validCreateDto;
    private ChapterResponseDto validResponseDto;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        validCreateDto = new ChapterCreateDto("Переменные", "Строки, числовые, логические переменные", 1L);
        validResponseDto = new ChapterResponseDto(1L, "Переменные", "Строки, числовые, логические переменные", 1, 1L, now, now);
    }

    //----------------------------------
    //Позитивные сценарии
    //----------------------------------

    @Test
    void create_Success_Returns201() throws Exception {
        Mockito.when(chapterService.create(Mockito.any(ChapterCreateDto.class))).thenReturn(validResponseDto);

        mockMvc.perform(post("/api/v1/chapters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(validResponseDto.id()))
                .andExpect(jsonPath("$.name").value(validResponseDto.name()))
                .andExpect(jsonPath("$.chapterOrder").value(validResponseDto.chapterOrder()))
                .andExpect(jsonPath("$.courseId").value(validResponseDto.courseId()));

        Mockito.verify(chapterService, Mockito.times(1)).create(Mockito.any(ChapterCreateDto.class));
    }

    @Test
    void getById_Success_Returns200() throws Exception {
        Long chapterId = 1L;
        Mockito.when(chapterService.getById(chapterId)).thenReturn(validResponseDto);

        mockMvc.perform(get("/api/v1/chapters/{id}", chapterId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(validResponseDto.id()))
                .andExpect(jsonPath("$.name").value(validResponseDto.name()));

        Mockito.verify(chapterService, Mockito.times(1)).getById(chapterId);
    }

    @Test
    void update_Success_Returns200() throws Exception {
        Long chapterId = 1L;
        ChapterCreateDto updateDto = new ChapterCreateDto("Generics", "Как работают дженерики", 1L);
        ChapterResponseDto updatedResponseDto = new ChapterResponseDto(chapterId, "Generics", "Как работают дженерики", 1, 1L, LocalDateTime.now(), LocalDateTime.now());

        Mockito.when(chapterService.update(Mockito.eq(chapterId), Mockito.any(ChapterCreateDto.class))).thenReturn(updatedResponseDto);

        mockMvc.perform(put("/api/v1/chapters/{id}", chapterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(chapterId))
                .andExpect(jsonPath("$.name").value("Generics"))
                .andExpect(jsonPath("$.description").value("Как работают дженерики"));

        Mockito.verify(chapterService, Mockito.times(1)).update(Mockito.eq(chapterId), Mockito.any(ChapterCreateDto.class));
    }

    @Test
    void delete_Success_Returns204() throws Exception {
        Long chapterId = 1L;
        Mockito.doNothing().when(chapterService).delete(chapterId);

        mockMvc.perform(delete("/api/v1/chapters/{id}", chapterId))
                .andExpect(status().isNoContent());

        Mockito.verify(chapterService, Mockito.times(1)).delete(chapterId);
    }

    @Test
    void getByCourseId_Success_Returns200() throws Exception {
        Long courseId = 1L;
        ChapterResponseDto chapter2 = new ChapterResponseDto(2L, "Циклы", "Описание 2", 2, courseId, LocalDateTime.now(), LocalDateTime.now());

        Mockito.when(chapterService.getByCourseId(courseId)).thenReturn(List.of(validResponseDto, chapter2));

        mockMvc.perform(get("/api/v1/chapters")
                        .param("courseId", String.valueOf(courseId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(validResponseDto.id()))
                .andExpect(jsonPath("$[0].name").value(validResponseDto.name()))
                .andExpect(jsonPath("$[1].id").value(chapter2.id()))
                .andExpect(jsonPath("$[1].name").value(chapter2.name()));

        Mockito.verify(chapterService, Mockito.times(1)).getByCourseId(courseId);
    }

    //----------------------------------
    //Негативные сценарии
    //----------------------------------

    @Test
    void create_InvalidDto_Returns400BadRequest() throws Exception {
        ChapterCreateDto invalidDto = new ChapterCreateDto("", "", -1L);

        mockMvc.perform(post("/api/v1/chapters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        Mockito.verify(chapterService, Mockito.never()).create(Mockito.any());
    }

    @Test
    void create_CourseNotFound_Returns404NotFound() throws Exception {
        Mockito.when(chapterService.create(Mockito.any(ChapterCreateDto.class)))
                .thenThrow(new EntityNotFoundException("Курс не найден"));

        mockMvc.perform(post("/api/v1/chapters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateDto)))
                .andExpect(status().isNotFound());

        Mockito.verify(chapterService, Mockito.times(1)).create(Mockito.any(ChapterCreateDto.class));
    }

    @Test
    void getById_NotFound_Returns404NotFound() throws Exception {
        Long nonExistentId = 99L;
        Mockito.when(chapterService.getById(nonExistentId))
                .thenThrow(new EntityNotFoundException("Глава не найдена"));

        mockMvc.perform(get("/api/v1/chapters/{id}", nonExistentId))
                .andExpect(status().isNotFound());

        Mockito.verify(chapterService, Mockito.times(1)).getById(nonExistentId);
    }

    @Test
    void update_InvalidDto_Returns400BadRequest() throws Exception {
        ChapterCreateDto invalidDto = new ChapterCreateDto(null, null, null);

        mockMvc.perform(put("/api/v1/chapters/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        Mockito.verify(chapterService, Mockito.never()).update(Mockito.anyLong(), Mockito.any());
    }

    @Test
    void update_NotFound_Returns404NotFound() throws Exception {
        Long nonExistentId = 99L;
        Mockito.when(chapterService.update(Mockito.eq(nonExistentId), Mockito.any(ChapterCreateDto.class)))
                .thenThrow(new EntityNotFoundException("Глава не найдена"));

        mockMvc.perform(put("/api/v1/chapters/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateDto)))
                .andExpect(status().isNotFound());

        Mockito.verify(chapterService, Mockito.times(1)).update(Mockito.eq(nonExistentId), Mockito.any(ChapterCreateDto.class));
    }

    @Test
    void delete_NotFound_Returns404NotFound() throws Exception {
        Long nonExistentId = 99L;
        Mockito.doThrow(new EntityNotFoundException("Глава не найдена"))
                .when(chapterService).delete(nonExistentId);

        mockMvc.perform(delete("/api/v1/chapters/{id}", nonExistentId))
                .andExpect(status().isNotFound());

        Mockito.verify(chapterService, Mockito.times(1)).delete(nonExistentId);
    }

    @Test
    void getByCourseId_CourseNotFound_Returns404NotFound() throws Exception {
        Long nonExistentCourseId = 99L;
        Mockito.when(chapterService.getByCourseId(nonExistentCourseId))
                .thenThrow(new EntityNotFoundException("Курс не найден"));

        mockMvc.perform(get("/api/v1/chapters")
                        .param("courseId", String.valueOf(nonExistentCourseId)))
                .andExpect(status().isNotFound());

        Mockito.verify(chapterService, Mockito.times(1)).getByCourseId(nonExistentCourseId);
    }
}