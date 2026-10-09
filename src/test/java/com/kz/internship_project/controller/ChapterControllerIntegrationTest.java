package com.kz.internship_project.controller;

import com.kz.internship_project.BaseIntegrationTest;
import com.kz.internship_project.config.KeycloakRoleConverter;
import com.kz.internship_project.dto.chapter.ChapterCreateDto;
import com.kz.internship_project.entity.Chapter;
import com.kz.internship_project.entity.Course;
import com.kz.internship_project.enums.RoleUser;
import com.kz.internship_project.repository.ChapterRepository;
import com.kz.internship_project.repository.CourseRepository;
import com.kz.internship_project.repository.LessonRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ChapterControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private LessonRepository lessonRepository;


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

    @BeforeEach
    void setUp() {

        lessonRepository.deleteAll();
        chapterRepository.deleteAll();
        courseRepository.deleteAll();
    }


    @Test
    @DisplayName("Должен успешно создать главу курса, если пользователь - ROLE_ADMIN")
    void create_ShouldReturn201_WhenUserIsAdminAndCourseExists() throws Exception {

        //Arrange
        Course course = new Course(null, "Java Developer", "Java Core, Spring Boot", null, null);
        courseRepository.save(course);
        ChapterCreateDto createDto = new ChapterCreateDto("Глава 1", "Описание", course.getId());

        //Act & Assert
        mockMvc.perform(post("/api/v1/chapters")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto))
                .with(jwtAdmin()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name", is("Глава 1")))
                .andExpect(jsonPath("$.description", is("Описание")))
                .andExpect(jsonPath("$.chapterOrder", is(1)))
                .andExpect(jsonPath("$.courseId", is(createDto.courseId().intValue())));

        assertEquals(1, chapterRepository.count());
    }

    @Test
    @DisplayName("Должен вернуть 404 Not Found, если курс не существует")
    void create_ShouldReturn404_WhenCourseDoesNotExist() throws Exception {

        //Arrange
        ChapterCreateDto createDto = new ChapterCreateDto("Глава 1", "Описание", 999L);
        //Act & Assert
        mockMvc.perform(post("/api/v1/chapters")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto))
                .with(jwtAdmin()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));

        assertEquals(0, chapterRepository.count());
    }

    @Test
    @DisplayName("Должен вернуть 403 Forbidden, если пользователь - ROLE_STUDENT")
    void create_ShouldReturn403_WhenUserIsStudent() throws Exception{

        //Arrange
        Course course = new Course(null, "Название курса", "Описание", null, null);
        courseRepository.save(course);
        ChapterCreateDto createDto = new ChapterCreateDto("Глава 1", "Описание", course.getId());

        //Act & Assert
        mockMvc.perform(post("/api/v1/chapters")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto))
                .with(jwtStudent()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));

        assertEquals(0, chapterRepository.count());

    }

    @Test
    @DisplayName("Должен вернуть 400 Bad Request, если DTO не прошло валидацию")
    void create_ShouldReturn400_WhenDtoIsInvalid() throws Exception {

        //Arrange
        Course course = new Course(null, "Название курса", "Описание", null, null);
        courseRepository.save(course);
        ChapterCreateDto createDto = new ChapterCreateDto("", "Описание", course.getId());

        //Act & Assert
        mockMvc.perform(post("/api/v1/chapters")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto))
                .with(jwtAdmin()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));

        assertEquals(0, chapterRepository.count());

    }

    @Test
    @DisplayName("Должен успешно вернуть главу по id, если пользователь - ROLE_STUDENT")
    void getById_ShouldReturnChapter_WhenUserIsStudent() throws Exception{

        //Arrange
        Course course = new Course(null, "Название", "Описание", null, null);
        Chapter chapter = new Chapter(null, "Название", "Описание", 1, course, null, null);
        courseRepository.save(course);
        chapterRepository.save(chapter);

        //Act & Assert
        mockMvc.perform(get("/api/v1/chapters/{id}", chapter.getId())
                .with(jwtStudent()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(chapter.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Название")))
                .andExpect(jsonPath("$.courseId", is(course.getId().intValue())));

    }

    @Test
    @DisplayName("Должен вернуть 404, если глава курса не существует. Пользователь - ROLE_STUDENT")
    void getById_ShouldReturn404_WhenChapterDoesNotExist() throws Exception {

        //Act & Assert
        mockMvc.perform(get("/api/v1/chapters/{id}", 999L)
                .with(jwtStudent()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));

    }

    @Test
    @DisplayName("Должен вернуть 403 Forbidden, если пользователь - ROLE_USER")
    void getById_ShouldReturn403_WhenUserIsPlainUser() throws Exception {

        //Arrange
        Course course = new Course(null, "Название", "Описание", null, null);
        Chapter chapter = new Chapter(null, "Название", "Описание", 1, course, null, null);
        courseRepository.save(course);
        chapterRepository.save(chapter);

        //Act & Assert
        mockMvc.perform(get("/api/v1/chapters/{id}", chapter.getId())
                .with(jwtUser()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    @Test
    @DisplayName("Должен успешно получить список глав по course_id, если курс существует. Пользователь - ROLE_STUDENT")
    void getByCourseId_ShouldReturnSortedChapters_WhenCourseExists() throws Exception{

        //Arrange
        Course course = new Course(null, "Название", "Описание", null, null);

        Chapter chapter1 = new Chapter(null,"Название 1", "Описание 1",1, course, null, null);
        Chapter chapter2 = new Chapter(null,"Название 2", "Описание 2",2, course, null, null);
        Chapter chapter3 = new Chapter(null,"Название 3", "Описание 3",3, course, null, null);
        courseRepository.save(course);
        chapterRepository.save(chapter1);
        chapterRepository.save(chapter2);
        chapterRepository.save(chapter3);

        //Act & Assert
        mockMvc.perform(get("/api/v1/chapters?courseId={courseId}", course.getId())
                .with(jwtStudent()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is(chapter1.getName())))
                .andExpect(jsonPath("$[1].name", is(chapter2.getName())))
                .andExpect(jsonPath("$[2].name", is(chapter3.getName())))
                .andExpect(jsonPath("$", Matchers.hasSize(3)));

        assertEquals(3, chapterRepository.count());
    }

    @Test
    @DisplayName("Должен вернуть 200 OK, если список глав пустой по course_id. Пользователь - ROLE_STUDENT")
    void getByCourseId_ShouldReturnEmptyList_WhenCourseHasNoChapters() throws Exception{

        //Arrange
        Course course = new Course(null, "Название", "Описание", null, null);
        courseRepository.save(course);

        //Act & Assert
        mockMvc.perform(get("/api/v1/chapters?courseId={courseId}",course.getId())
                .with(jwtStudent()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

    }

    @Test
    @DisplayName("Должен вернуть 404 Not Found, если курс не существует. Пользователь - ROLE_STUDENT")
    void getByCourseId_ShouldReturn404_WhenCourseDoesNotExist() throws Exception{

        //Act & Assert
        mockMvc.perform(get("/api/v1/chapters?courseId={courseId}", 999L)
                .with(jwtStudent()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));

    }

    @Test
    @DisplayName("Должен вернуть 200 OK, если пользователь - ROLE_ADMIN")
    void update_ShouldUpdateChapter_WhenUserIsAdmin() throws Exception{

        //Arrange
        Course course = new Course(null, "Название", "Описание", null, null);
        courseRepository.save(course);

        Chapter chapter = new Chapter(null, "Название", "Описание", 1, course, null, null);
        chapterRepository.save(chapter);

        ChapterCreateDto updateDto = new ChapterCreateDto("Название обновленное", "Описание обновленное", course.getId());

        //Act & Assert
        mockMvc.perform(put("/api/v1/chapters/{id}", chapter.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
                .with(jwtAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(updateDto.name())))
                .andExpect(jsonPath("$.description", is(updateDto.description())));

        Chapter updatedChapter = chapterRepository.findById(chapter.getId()).orElseThrow();
        assertEquals("Название обновленное", updatedChapter.getName());
        assertEquals("Описание обновленное", updatedChapter.getDescription());    }

    @Test
    @DisplayName("Должен вернуть 404 Not Found, если глава не существует. Пользователь - ROLE_ADMIN")
    void update_ShouldReturn404_WhenChapterDoesNotExist() throws Exception{

        //Arrange
        Course course = new Course(null, "Название", "Описание", null, null);
        courseRepository.save(course);
        ChapterCreateDto updateDto = new ChapterCreateDto("Название обновленное", "Описание обновленное", course.getId());

        //Act & Assert
        mockMvc.perform(put("/api/v1/chapters/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
                .with(jwtAdmin()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));


    }


    @Test
    @DisplayName("Должен вернуть 403 Forbidden, если пользователь - ROLE_STUDENT")
    void delete_ShouldReturn403_WhenUserIsStudent() throws Exception {

        //Arrange
        Course course = new Course(null, "Название", "Описание", null, null);
        courseRepository.save(course);
        Chapter chapter = new Chapter(null, "Название", "Описание", 1, course, null, null);
        chapterRepository.save(chapter);

        //Act & Assert
        mockMvc.perform(delete("/api/v1/chapters/{id}", chapter.getId())
                .with(jwtStudent()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));

        assertEquals(1, chapterRepository.count());
    }
}
