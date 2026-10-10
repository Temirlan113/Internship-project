package com.kz.internship_project.controller;

import com.kz.internship_project.BaseIntegrationTest;
import com.kz.internship_project.config.KeycloakRoleConverter;
import com.kz.internship_project.dto.lesson.LessonCreateDto;
import com.kz.internship_project.entity.Chapter;
import com.kz.internship_project.entity.Course;
import com.kz.internship_project.entity.Lesson;
import com.kz.internship_project.enums.RoleUser;
import com.kz.internship_project.repository.ChapterRepository;
import com.kz.internship_project.repository.CourseRepository;
import com.kz.internship_project.repository.LessonRepository;
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

public class LessonControllerIntegrationTest extends BaseIntegrationTest {

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
    @DisplayName("Должен успешно создать урок, если глава урока существует и пользователь - ROLE_ADMIN")
    void create_ShouldReturn201_WhenUserIsAdminAndChapterExists() throws Exception {

        //Arrange
        Course course = new Course(null, "Java Developer", "Java Core, Spring Boot", null, null);
        courseRepository.save(course);
        Chapter chapter = new Chapter(null, "Название главы", "Описание главы", 1, course, null, null);
        chapterRepository.save(chapter);
        LessonCreateDto createDto = new LessonCreateDto("Название урока", "Описание урока", "Контент", chapter.getId());

        //Act & Assert
        mockMvc.perform(post("/api/v1/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto))
                        .with(jwtAdmin()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name", is("Название урока")))
                .andExpect(jsonPath("$.description", is("Описание урока")))
                .andExpect(jsonPath("$.content", is("Контент")))
                .andExpect(jsonPath("$.lessonOrder", is(1)))
                .andExpect(jsonPath("$.chapterId", is(createDto.chapterId().intValue())));

        assertEquals(1, lessonRepository.count());
    }

    @Test
    @DisplayName("Должен вернуть 404 Not Found, когда глава курса не существует. Пользователь - ROLE_ADMIN")
    void create_ShouldReturn404_WhenChapterDoesNotExist() throws Exception {
        //Arrange
        LessonCreateDto createDto = new LessonCreateDto("Название урока", "Описание урока", "Контент", 999L);

        //Act & Assert
        mockMvc.perform(post("/api/v1/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto))
                        .with(jwtAdmin()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));

        assertEquals(0, lessonRepository.count());

    }

    @Test
    @DisplayName("Должен вернуть 403 Forbidden, когда пользователь - ROLE_STUDENT")
    void create_ShouldReturn403_WhenUserIsStudent() throws Exception {
        //Arrange
        Course course = new Course(null, "Java Developer", "Java Core, Spring Boot", null, null);
        courseRepository.save(course);
        Chapter chapter = new Chapter(null, "Название главы", "Описание главы", 1, course, null, null);
        chapterRepository.save(chapter);
        LessonCreateDto createDto = new LessonCreateDto("Название урока", "Описание урока", "Контент", chapter.getId());

        //Act & Assert
        mockMvc.perform(post("/api/v1/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto))
                        .with(jwtStudent()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));

        assertEquals(0, lessonRepository.count());
    }

    @Test
    @DisplayName("Должен вернуть 400 Bad Request, если DTO не прошло валидацию. Пользователь - ROLE_ADMIN")
    void create_ShouldReturn400_WhenDtoIsInvalid() throws Exception {

        //Arrange
        LessonCreateDto createDto = new LessonCreateDto("", "Описание урока", "Контент", -500L);

        //Act & Assert
        mockMvc.perform(post("/api/v1/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto))
                        .with(jwtAdmin()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));

        assertEquals(0, lessonRepository.count());

    }

    @Test
    @DisplayName("Должен успешно вернуть id урока, если пользователь - ROLE_STUDENT")
    void getById_ShouldReturnLesson_WhenUserIsStudent() throws Exception {

        //Arrange
        Course course = new Course(null, "Java Developer", "Java Core, Spring Boot", null, null);
        courseRepository.save(course);
        Chapter chapter = new Chapter(null, "Название главы", "Описание главы", 1, course, null, null);
        chapterRepository.save(chapter);
        Lesson lesson = new Lesson(null, "Название урока", "Описание урока", "Контент урока", 1, chapter, null, null);
        lessonRepository.save(lesson);
        //Act & Assert
        mockMvc.perform(get("/api/v1/lessons/{id}", lesson.getId())
                        .with(jwtStudent()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(lesson.getId().intValue())))
                .andExpect(jsonPath("$.name", is(lesson.getName())))
                .andExpect(jsonPath("$.content", is(lesson.getContent())))
                .andExpect(jsonPath("$.chapterId", is(lesson.getChapter().getId().intValue())));

    }

    @Test
    @DisplayName("Должен вернуть 404 Not Found, если урок не существует. Пользователь - ROLE_ADMIN")
    void getById_ShouldReturn404_WhenLessonDoesNotExist() throws Exception {

        //Act & Assert
        mockMvc.perform(get("/api/v1/lessons/{id}", 999L)
                        .with(jwtStudent()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("Должен вернуть 403 Forbidden, если пользователь - ROLE_USER")
    void getById_ShouldReturn403_WhenUserIsRoleUser() throws Exception {

        //Arrange
        Course course = new Course(null, "Java Developer", "Java Core, Spring Boot", null, null);
        courseRepository.save(course);
        Chapter chapter = new Chapter(null, "Название главы", "Описание главы", 1, course, null, null);
        chapterRepository.save(chapter);
        Lesson lesson = new Lesson(null, "Название урока", "Описание урока", "Контент урока", 1, chapter, null, null);
        lessonRepository.save(lesson);

        //Act & Assert
        mockMvc.perform(get("/api/v1/lessons/{id}", lesson.getId())
                        .with(jwtUser()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    @Test
    @DisplayName("Должен вернуть список уроков по chapterId, если глава курса существует. Пользователь - ROLE_STUDENT")
    void getByChapterId_ShouldReturnSortedLessons_WhenChapterExists() throws Exception {

        //Arrange
        Course course = new Course(null, "Java Developer", "Java Core, Spring Boot", null, null);
        courseRepository.save(course);
        Chapter chapter = new Chapter(null, "Название главы", "Описание главы", 1, course, null, null);
        chapterRepository.save(chapter);
        Lesson lesson1 = new Lesson(null, "Название урока", "Описание урока", "Контент урока", 1, chapter, null, null);
        lessonRepository.save(lesson1);
        Lesson lesson2 = new Lesson(null, "Название урока", "Описание урока", "Контент урока", 2, chapter, null, null);
        lessonRepository.save(lesson2);
        Lesson lesson3 = new Lesson(null, "Название урока", "Описание урока", "Контент урока", 3, chapter, null, null);
        lessonRepository.save(lesson3);

        //Act & Assert
        mockMvc.perform(get("/api/v1/lessons?chapterId={chapterId}", chapter.getId())
                        .with(jwtStudent()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is(lesson1.getName())))
                .andExpect(jsonPath("$[1].name", is(lesson2.getName())))
                .andExpect(jsonPath("$[2].name", is(lesson3.getName())))
                .andExpect(jsonPath("$", hasSize(3)));


        assertEquals(3, lessonRepository.count());
    }

    @Test
    @DisplayName("Должен вернуть пустой список уроков по chapterId, если у главы нет уроков. Пользователь - ROLE_STUDENT")
    void getByChapterId_ShouldReturnEmptyList_WhenChapterHasNoLessons() throws Exception {
        //Arrange
        Course course = new Course(null, "Java Developer", "Java Core, Spring Boot", null, null);
        courseRepository.save(course);
        Chapter chapter = new Chapter(null, "Название главы", "Описание главы", 1, course, null, null);
        chapterRepository.save(chapter);

        //Act & Assert
        mockMvc.perform(get("/api/v1/lessons?chapterId={chapterId}", chapter.getId())
                        .with(jwtStudent()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        assertEquals(0, lessonRepository.count());

    }

    @Test
    @DisplayName("Должен обновить урок, если пользователь - ROLE_ADMIN")
    void update_ShouldUpdateLesson_WhenUserIsAdmin() throws Exception {

        //Arrange
        Course course = new Course(null, "Java Developer", "Java Core, Spring Boot", null, null);
        courseRepository.save(course);
        Chapter chapter = new Chapter(null, "Название главы", "Описание главы", 1, course, null, null);
        chapterRepository.save(chapter);
        Lesson lesson = new Lesson(null, "Название урока", "Описание урока", "Контент урока", 1, chapter, null, null);
        lessonRepository.save(lesson);
        LessonCreateDto updateDto = new LessonCreateDto("Обновленное название урока", "Обновленное описание урока", "Обновленный контент", chapter.getId());

        //Act & Assert
        mockMvc.perform(put("/api/v1/lessons/{id}", lesson.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .with(jwtAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(updateDto.name())))
                .andExpect(jsonPath("$.description", is(updateDto.description())))
                .andExpect(jsonPath("$.content", is(updateDto.content())))
                .andExpect(jsonPath("$.chapterId", is(updateDto.chapterId().intValue())));

        Lesson updatedLesson = lessonRepository.findById(lesson.getId()).orElseThrow();

        assertEquals("Обновленное название урока", updatedLesson.getName());
        assertEquals("Обновленное описание урока", updatedLesson.getDescription());
        assertEquals("Обновленный контент", updatedLesson.getContent());
        assertEquals(chapter.getId(), updatedLesson.getChapter().getId());
    }

    @Test
    @DisplayName("Должен вернуть 404 Not Found, если урока не существует. Пользователь - ROLE_ADMIN")
    void update_ShouldReturn404_WhenLessonDoesNotExist() throws Exception{

        //Arrange
        Course course = new Course(null, "Java Developer", "Java Core, Spring Boot", null, null);
        courseRepository.save(course);
        Chapter chapter = new Chapter(null, "Название главы", "Описание главы", 1, course, null, null);
        chapterRepository.save(chapter);
        LessonCreateDto updateDto = new LessonCreateDto("Обновленное название урока", "Обновленное описание урока", "Обновленный контент", chapter.getId());


        //Act & Assert
        mockMvc.perform(put("/api/v1/lessons/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto))
                .with(jwtAdmin()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));

    }


}
