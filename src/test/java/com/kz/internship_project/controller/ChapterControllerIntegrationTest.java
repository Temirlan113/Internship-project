package com.kz.internship_project.controller;

import com.kz.internship_project.BaseIntegrationTest;
import com.kz.internship_project.dto.chapter.ChapterCreateDto;
import com.kz.internship_project.entity.Chapter;
import com.kz.internship_project.repository.ChapterRepository;
import com.kz.internship_project.repository.LessonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ChapterControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @BeforeEach
    void setUp(){
        lessonRepository.deleteAll();
        chapterRepository.deleteAll();
    }

    @Test
    @DisplayName("Должен успешно создать главу курса, если пользователь - ROLE_ADMIN")
    void create_ShouldReturn201_WhenUserIsAdmin() throws Exception{
        //Arrange
        ChapterCreateDto requestDto = new ChapterCreateDto("Java Memory", "Память Java", 1L);

        //Act & Assert
        mockMvc.perform(post("/api/v1/chapters")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name", is("Java Memory")))
                .andExpect(jsonPath("$.description", is("Память Java")))
                .andExpect(jsonPath("$.courseId", is(1L)))
                .andExpect(jsonPath("$.createdTime").exists());

        List<Chapter> chapters = chapterRepository.findAll();
        assertEquals(1, chapters.size());

        Chapter savedChapter = chapters.get(0);
        assertEquals("Java Memory", savedChapter.getName());
        assertEquals("Память Java", savedChapter.getDescription());
        assertNotNull(savedChapter.getId());



    }
}
