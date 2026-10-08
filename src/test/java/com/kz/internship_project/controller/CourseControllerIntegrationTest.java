package com.kz.internship_project.controller;

import com.kz.internship_project.BaseIntegrationTest;
import com.kz.internship_project.dto.course.CourseCreateDto;
import com.kz.internship_project.entity.Course;
import com.kz.internship_project.repository.ChapterRepository;
import com.kz.internship_project.repository.CourseRepository;
import com.kz.internship_project.repository.LessonRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CourseControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @BeforeEach
    void setUp() {
        // Очищаем БД перед каждым тестом, чтобы обеспечить изоляцию
        lessonRepository.deleteAll();
        chapterRepository.deleteAll();
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
                        .with(jwt().jwt(jwt -> jwt.claim("realm_access", Map.of("roles", List.of("ADMIN")))))).andExpect(status().isCreated())
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
                        .with(jwt().jwt(jwt -> jwt.claim("realm_access", Map.of("roles", List.of("ROLE_USER"))))))
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
                        .with(jwt().jwt(jwt -> jwt.claim("realm_access", Map.of("roles", List.of("ROLE_ADMIN"))))))
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
                        .with(jwt().jwt(jwt -> jwt.claim("realm_access", Map.of("roles", List.of("ROLE_STUDENT"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedCourse.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Spring Boot 3")));
    }

    @Test
    @DisplayName("Должен успешно получить список с пагинацией")
    void getAll_ShouldReturnPagedCourses_WhenUserIsStudent() throws Exception {

        //Arrange
        Course course1 = new Course(null, "Course A", "Описание А", null, null);
        Course course2 = new Course(null, "Course B", "Описание B", null, null);
        Course course3 = new Course(null, "Course C", "Описание C", null, null);


        courseRepository.saveAll(List.of(course1, course2, course3));

        //Act & Assert
        mockMvc.perform(get("/api/v1/courses")
                        .param("page", "0")
                        .param("size", "2")
                        .with(jwt().jwt(jwt -> jwt.claim("realm_access", Map.of("roles", List.of("ROLE_STUDENT"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", Matchers.hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(3)));
    }

    @Test
    @DisplayName("Должен вернуть 404 Not Found, когда курс отсутствует")
    void getById_ShouldReturn404_WhenCourseDoesNotExist() throws Exception {
        //Act & Assert
        mockMvc.perform(get("/api/v1/courses/{id}", 999L)
                        .with(jwt().jwt(jwt -> jwt.claim("realm_access", Map.of("roles", List.of("ROLE_STUDENT"))))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("Должен успешно обновить курс, если пользователь - ROLE_ADMIN")
    void update_ShouldUpdateCourse_WhenUserIsAdmin() throws Exception {

        //Arrange
        Course savedCourse = new Course(null, "Old name", "Description", null, null);
        CourseCreateDto createDto = new CourseCreateDto("New name", "New Description");

        courseRepository.save(savedCourse);
        //Act & Assert
        mockMvc.perform(put("/api/v1/courses/{id}", savedCourse.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto))
                        .with(jwt().jwt(jwt -> jwt.claim("realm_access", Map.of("roles", List.of("ROLE_ADMIN"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("New name")))
                .andExpect(jsonPath("$.description", is("New Description")));


        Course updatedDbCourse = courseRepository.findById(savedCourse.getId()).orElseThrow();
        assertEquals("New name", updatedDbCourse.getName());
        assertEquals("New Description", updatedDbCourse.getDescription());
    }

    @Test
    @DisplayName("Должен вернуть 404 Not Found при попытке обновить несуществующий курс")
    void update_ShouldReturn404_WhenCourseDoesNotExist() throws Exception {
        CourseCreateDto updateDto = new CourseCreateDto("New name", "New Description");


        //Act & Assert
        mockMvc.perform(put("/api/v1/courses/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto))
                .with(jwt().jwt(jwt -> jwt.claim("realm_access", Map.of("roles", List.of("ROLE_ADMIN"))))))
                .andExpect(status().isNotFound());


        assertEquals(0, courseRepository.count());

    }

    @Test
    @DisplayName("Должен вернуть 403 Forbidden при попытке удалить курс, если пользователь - ROLE_STUDENT")
    void delete_ShouldReturn403_WhenUserIsStudent() throws Exception {

        //Arrange
        Course course = new Course(null, "Spring Boot 3", "Продвинутый курс", null, null);
        Course savedCourse = courseRepository.save(course);

        //Act & Assert
        mockMvc.perform(delete("/api/v1/courses/{id}", savedCourse.getId())
                .with(jwt().jwt(jwt -> jwt.claim("realm_access", Map.of("roles", List.of("ROLE_STUDENT"))))))
                .andExpect(status().isForbidden());

        assertEquals(1, courseRepository.count());
    }

    @Test
    @DisplayName("Должен вернуть 400 Bad Request, если превышен лимит символов(256+)")
    void create_ShouldReturn400_WhenNameExceedsMaxLength() throws Exception {

        //Arrange
        CourseCreateDto courseCreateDto = new CourseCreateDto("A".repeat(256), "Описание");


        //Act & Assert
        mockMvc.perform(post("/api/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseCreateDto))
                        .with(jwt().jwt(jwt -> jwt.claim("realm_access", Map.of("roles", List.of("ROLE_ADMIN"))))))
                .andExpect(status().isBadRequest());

        assertEquals(0, courseRepository.count());
    }


}
