package com.kz.internship_project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kz.internship_project.dto.CourseCreateDto;
import com.kz.internship_project.dto.CourseResponseDto;
import com.kz.internship_project.service.CourseService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseController.class)
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;


    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CourseService courseService;

    @Test
    void create_Success_Returns201() throws Exception {
        // Arrange
        CourseCreateDto createDto = new CourseCreateDto("Java Core", "Все про Java");
        CourseResponseDto responseDto = new CourseResponseDto(1L, "Java Core", "Все про Java", LocalDateTime.now(),LocalDateTime.now());

        Mockito.when(courseService.create(Mockito.any(CourseCreateDto.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Java Core"))
                .andExpect(jsonPath("$.description").value("Все про Java"));

        Mockito.verify(courseService, Mockito.times(1)).create(Mockito.any(CourseCreateDto.class));
    }

    @Test
    void getById_Success_Returns200() throws Exception {
        // Arrange
        Long courseId = 1L;
        CourseResponseDto responseDto = new CourseResponseDto(courseId, "Java Core", "Все про Java", LocalDateTime.now(), LocalDateTime.now());

        Mockito.when(courseService.getById(courseId)).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(get("/api/v1/courses/{id}", courseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Java Core"));

        Mockito.verify(courseService, Mockito.times(1)).getById(courseId);
    }

    @Test
    void update_Success_Returns200() throws Exception {
        // Arrange
        Long courseId = 1L;
        CourseCreateDto updateDto = new CourseCreateDto("Spring Boot", "Продвинутый курс");
        CourseResponseDto responseDto = new CourseResponseDto(courseId, "Spring Boot", "Продвинутый курс", LocalDateTime.now(), LocalDateTime.now());

        Mockito.when(courseService.update(Mockito.eq(courseId), Mockito.any(CourseCreateDto.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(put("/api/v1/courses/{id}", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
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
        CourseResponseDto course1 = new CourseResponseDto(1L, "Java Core", "Все про Java", LocalDateTime.now(), LocalDateTime.now());
        CourseResponseDto course2 = new CourseResponseDto(2L, "Spring Core", "Все про Spring", LocalDateTime.now(), LocalDateTime.now());
        Page<CourseResponseDto> pageResponse = new PageImpl<>(List.of(course1, course2));

        Mockito.when(courseService.getAll(0, 10, "id", "asc")).thenReturn(pageResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/courses")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "id")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].name").value("Java Core"))
                .andExpect(jsonPath("$.content[1].id").value(2L))
                .andExpect(jsonPath("$.content[1].name").value("Spring Core"));

        Mockito.verify(courseService, Mockito.times(1)).getAll(0, 10, "id", "asc");
    }
}