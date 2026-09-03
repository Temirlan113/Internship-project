package com.kz.internship_project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kz.internship_project.config.JacksonConfig;
import com.kz.internship_project.dto.course.CourseCreateDto;
import com.kz.internship_project.dto.course.CourseResponseDto;
import com.kz.internship_project.exception.GlobalExceptionHandler;
import com.kz.internship_project.service.CourseService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = CourseController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class, OAuth2ResourceServerAutoConfiguration.class})
@Import({GlobalExceptionHandler.class, JacksonConfig.class})
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CourseService courseService;

    @Autowired
    private ObjectMapper objectMapper;

    private CourseCreateDto validCreateDto;
    private CourseResponseDto validResponseDto;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        validCreateDto = new CourseCreateDto("Java Core", "Все про Java");
        validResponseDto = new CourseResponseDto(1L, "Java Core", "Все про Java", now, now);
    }

    //----------------------------------
    // Позитивные сценарии
    //----------------------------------

    @Test
    void create_Success_Returns201() throws Exception {
        // Arrange
        Mockito.when(courseService.create(Mockito.any(CourseCreateDto.class))).thenReturn(validResponseDto);

        // Act & Assert
        mockMvc.perform(post("/api/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(validResponseDto.id()))
                .andExpect(jsonPath("$.name").value(validResponseDto.name()))
                .andExpect(jsonPath("$.description").value(validResponseDto.description()));

        Mockito.verify(courseService, Mockito.times(1)).create(Mockito.any(CourseCreateDto.class));
    }

    @Test
    void getById_Success_Returns200() throws Exception {
        // Arrange
        Long courseId = 1L;
        Mockito.when(courseService.getById(courseId)).thenReturn(validResponseDto);

        // Act & Assert
        mockMvc.perform(get("/api/v1/courses/{id}", courseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(validResponseDto.id()))
                .andExpect(jsonPath("$.name").value(validResponseDto.name()));

        Mockito.verify(courseService, Mockito.times(1)).getById(courseId);
    }

    @Test
    void update_Success_Returns200() throws Exception {
        // Arrange
        Long courseId = 1L;
        CourseCreateDto updateDto = new CourseCreateDto("Spring Boot", "Продвинутый курс");
        CourseResponseDto updatedResponseDto = new CourseResponseDto(courseId, "Spring Boot", "Продвинутый курс", LocalDateTime.now(), LocalDateTime.now());

        Mockito.when(courseService.update(Mockito.eq(courseId), Mockito.any(CourseCreateDto.class))).thenReturn(updatedResponseDto);

        // Act & Assert
        mockMvc.perform(put("/api/v1/courses/{id}", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(courseId))
                .andExpect(jsonPath("$.name").value("Spring Boot"))
                .andExpect(jsonPath("$.description").value("Продвинутый курс"));

        Mockito.verify(courseService, Mockito.times(1)).update(Mockito.eq(courseId), Mockito.any(CourseCreateDto.class));
    }

    @Test
    void delete_Success_Returns204() throws Exception {
        // Arrange
        Long courseId = 1L;
        Mockito.doNothing().when(courseService).delete(courseId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/courses/{id}", courseId))
                .andExpect(status().isNoContent());

        Mockito.verify(courseService, Mockito.times(1)).delete(courseId);
    }

    @Test
    void getAll_Success_Returns200() throws Exception {
        // Arrange
        CourseResponseDto course2 = new CourseResponseDto(2L, "Spring Core", "Все про Spring", LocalDateTime.now(), LocalDateTime.now());
        Page<CourseResponseDto> pageResponse = new PageImpl<>(List.of(validResponseDto, course2));

        Mockito.when(courseService.getAll(0, 10, "id", "asc")).thenReturn(pageResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/courses")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "id")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(validResponseDto.id()))
                .andExpect(jsonPath("$.content[0].name").value(validResponseDto.name()))
                .andExpect(jsonPath("$.content[1].id").value(course2.id()))
                .andExpect(jsonPath("$.content[1].name").value(course2.name()));

        Mockito.verify(courseService, Mockito.times(1)).getAll(0, 10, "id", "asc");
    }

    //----------------------------------
    // Негативные сценарии
    //----------------------------------

    @Test
    void create_InvalidDto_Returns400BadRequest() throws Exception {
        // Arrange
        CourseCreateDto invalidDto = new CourseCreateDto("", "");

        // Act & Assert
        mockMvc.perform(post("/api/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        Mockito.verify(courseService, Mockito.never()).create(Mockito.any());
    }

    @Test
    void getById_NotFound_Returns404NotFound() throws Exception {
        // Arrange
        Long nonExistentId = 99L;
        Mockito.when(courseService.getById(nonExistentId))
                .thenThrow(new EntityNotFoundException("Курс с id " + nonExistentId + " не найден"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/courses/{id}", nonExistentId))
                .andExpect(status().isNotFound());

        Mockito.verify(courseService, Mockito.times(1)).getById(nonExistentId);
    }

    @Test
    void update_InvalidDto_Returns400BadRequest() throws Exception {
        // Arrange
        CourseCreateDto invalidDto = new CourseCreateDto("   ", null);

        // Act & Assert
        mockMvc.perform(put("/api/v1/courses/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        Mockito.verify(courseService, Mockito.never()).update(Mockito.anyLong(), Mockito.any());
    }

    @Test
    void update_NotFound_Returns404NotFound() throws Exception {
        // Arrange
        Long nonExistentId = 99L;
        Mockito.when(courseService.update(Mockito.eq(nonExistentId), Mockito.any(CourseCreateDto.class)))
                .thenThrow(new EntityNotFoundException("Курс с id " + nonExistentId + " не найден"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/courses/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateDto)))
                .andExpect(status().isNotFound());

        Mockito.verify(courseService, Mockito.times(1)).update(Mockito.eq(nonExistentId), Mockito.any(CourseCreateDto.class));
    }

    @Test
    void delete_NotFound_Returns404NotFound() throws Exception {
        // Arrange
        Long nonExistentId = 99L;
        Mockito.doThrow(new EntityNotFoundException("Курс с id " + nonExistentId + " не найден"))
                .when(courseService).delete(nonExistentId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/courses/{id}", nonExistentId))
                .andExpect(status().isNotFound());

        Mockito.verify(courseService, Mockito.times(1)).delete(nonExistentId);
    }

    @Test
    void delete_CourseHasChapters_Returns400BadRequest() throws Exception {
        // Arrange
        Long courseId = 1L;
        Mockito.doThrow(new IllegalArgumentException("Курс нельзя удалить, пока в нем есть главы!"))
                .when(courseService).delete(courseId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/courses/{id}", courseId))
                .andExpect(status().isBadRequest());

        Mockito.verify(courseService, Mockito.times(1)).delete(courseId);
    }
}