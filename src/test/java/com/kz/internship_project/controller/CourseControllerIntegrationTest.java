package com.kz.internship_project.controller;

import com.kz.internship_project.BaseIntegrationTest;
import com.kz.internship_project.dto.course.CourseCreateDto;
import com.kz.internship_project.entity.Course;
import com.kz.internship_project.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;

import static com.kz.internship_project.utils.SecurityTestUtils.keycloakJwt;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CourseControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CourseRepository courseRepository;

    @BeforeEach
    void setUp() {
        // Очищаем БД перед каждым тестом, чтобы обеспечить изоляцию
        courseRepository.deleteAll();
    }

    @Test
    @DisplayName("Должен успешно создать курс, если пользователь — ROLE_ADMIN")
    void create_ShouldReturn201_WhenUserIsAdmin() throws Exception {
        // Arrange (Готовим данные)
        CourseCreateDto requestDto = new CourseCreateDto("Java Core", "Основы языка Java");

        // Act & Assert HTTP level (Имитируем запрос с ролями Keycloak)
        mockMvc.perform(post("/api/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(keycloakJwt("ROLE_ADMIN"))) // Подставляем валидный токен с ролью ADMIN
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name", is("Java Core")))
                .andExpect(jsonPath("$.description", is("Основы языка Java")))
                .andExpect(jsonPath("$.createdTime").exists());

        // Assert DB level (ПРОВЕРКА БАЗЫ ДАННЫХ — ключ к настоящему интеграционному тесту)
        List<Course> courses = courseRepository.findAll();
        assertEquals(1, courses.size());

        Course savedCourse = courses.get(0);
        assertEquals("Java Core", savedCourse.getName());
        assertEquals("Основы языка Java", savedCourse.getDescription());
        assertNotNull(savedCourse.getId());
    }

    @Test
    @DisplayName("Должен вернуть 403 Forbidden, если курс пытается создать ROLE_USER")
    void create_ShouldReturn403_WhenUserIsNotAdmin() throws Exception {
        // Arrange
        CourseCreateDto requestDto = new CourseCreateDto("Java Core", "Основы языка Java");

        // Act & Assert
        mockMvc.perform(post("/api/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(keycloakJwt("ROLE_USER"))) // Попытка от обычного пользователя
                .andExpect(status().isForbidden());

        // Проверяем, что в БД ничего не сохранилось
        assertEquals(0, courseRepository.count());
    }

    @Test
    @DisplayName("Должен вернуть 400 Bad Request, если передано пустое имя курса")
    void create_ShouldReturn400_WhenNameIsBlank() throws Exception {
        // Arrange (Нарушаем валидацию @NotBlank)
        CourseCreateDto invalidDto = new CourseCreateDto("", "Описание без названия");

        // Act & Assert
        mockMvc.perform(post("/api/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .with(keycloakJwt("ROLE_ADMIN")))
                .andExpect(status().isBadRequest());

        // Проверяем, что в базе пусто
        assertEquals(0, courseRepository.count());
    }

    @Test
    @DisplayName("Должен успешно вернуть курс по ID для ROLE_STUDENT")
    void getById_ShouldReturnCourse_WhenUserIsStudent() throws Exception {
        // Arrange: Сначала добавляем курс прямо в базу через репозиторий
        Course course = new Course();
        course.setName("Spring Boot 3");
        course.setDescription("Продвинутый курс");
        Course savedCourse = courseRepository.save(course);

        // Act & Assert: Запрашиваем с ролью STUDENT (в SecurityConfig разрешен просмотр)
        mockMvc.perform(get("/api/v1/courses/{id}", savedCourse.getId())
                        .with(keycloakJwt("ROLE_STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedCourse.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Spring Boot 3")));
    }
}
