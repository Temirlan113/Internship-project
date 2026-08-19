package com.kz.internship_project.service.impl;

import com.kz.internship_project.dto.LessonCreateDto;
import com.kz.internship_project.dto.LessonResponseDto;
import com.kz.internship_project.entity.Chapter;
import com.kz.internship_project.entity.Lesson;
import com.kz.internship_project.mapper.LessonMapper;
import com.kz.internship_project.repository.ChapterRepository;
import com.kz.internship_project.repository.LessonRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class LessonServiceImplTest {

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private ChapterRepository chapterRepository;

    @Spy
    private LessonMapper lessonMapper = Mappers.getMapper(LessonMapper.class);

    @InjectMocks
    private LessonServiceImpl lessonService;

    @Test
    void create_Success() {
        // Arrange
        Chapter fakeChapter = new Chapter(1L, "Переменные", "Описание главы", 1, null, LocalDateTime.now(), LocalDateTime.now());
        LessonCreateDto dto = new LessonCreateDto("Строки", "Урок про строки", "Текст урока...", 1L);
        Lesson savedLesson = new Lesson(1L, "Строки", "Урок про строки", "Текст урока...", 1, fakeChapter, LocalDateTime.now(), LocalDateTime.now());

        Mockito.when(chapterRepository.findById(dto.chapterId())).thenReturn(Optional.of(fakeChapter));
        Mockito.when(lessonRepository.findFirstByChapterIdOrderByLessonOrderDesc(dto.chapterId())).thenReturn(Optional.empty());
        Mockito.when(lessonRepository.save(Mockito.any(Lesson.class))).thenReturn(savedLesson);

        // Act
        LessonResponseDto result = lessonService.create(dto);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1L, result.id());
        Assertions.assertEquals("Строки", result.name());
        Assertions.assertEquals("Урок про строки", result.description());
        Assertions.assertEquals("Текст урока...", result.content());

        Mockito.verify(chapterRepository, Mockito.times(1)).findById(dto.chapterId());
        Mockito.verify(lessonRepository, Mockito.times(1)).findFirstByChapterIdOrderByLessonOrderDesc(dto.chapterId());
        Mockito.verify(lessonRepository, Mockito.times(1)).save(Mockito.any(Lesson.class));
    }

    @Test
    void getById_Success() {
        // Arrange
        Chapter fakeChapter = new Chapter(1L, "Переменные", "Описание главы", 1, null, LocalDateTime.now(), LocalDateTime.now());
        Lesson fakeLesson = new Lesson(1L, "Строки", "Урок про строки", "Текст урока...", 1, fakeChapter, LocalDateTime.now(), LocalDateTime.now());

        Mockito.when(lessonRepository.findById(fakeLesson.getId())).thenReturn(Optional.of(fakeLesson));

        // Act
        LessonResponseDto result = lessonService.getById(fakeLesson.getId());

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(fakeLesson.getId(), result.id());
        Assertions.assertEquals(fakeLesson.getName(), result.name());

        Mockito.verify(lessonRepository, Mockito.times(1)).findById(fakeLesson.getId());
    }

    @Test
    void update_Success() {
        // Arrange
        Chapter fakeChapter = new Chapter(1L, "Переменные", "Описание главы", 1, null, LocalDateTime.now(), LocalDateTime.now());
        Lesson oldLesson = new Lesson(1L, "Старое имя", "Старое описание", "Старый контент", 1, fakeChapter, LocalDateTime.now(), LocalDateTime.now());
        LessonCreateDto updateDto = new LessonCreateDto("Числа", "Урок про числа", "Новый текст...", 1L);

        Mockito.when(chapterRepository.findById(updateDto.chapterId())).thenReturn(Optional.of(fakeChapter));
        Mockito.when(lessonRepository.findById(oldLesson.getId())).thenReturn(Optional.of(oldLesson));
        Mockito.when(lessonRepository.save(Mockito.any(Lesson.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        LessonResponseDto result = lessonService.update(oldLesson.getId(), updateDto);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(updateDto.name(), result.name());
        Assertions.assertEquals(updateDto.description(), result.description());
        Assertions.assertEquals(updateDto.content(), result.content());

        Mockito.verify(chapterRepository, Mockito.times(1)).findById(updateDto.chapterId());
        Mockito.verify(lessonRepository, Mockito.times(1)).findById(oldLesson.getId());
        Mockito.verify(lessonRepository, Mockito.times(1)).save(oldLesson);
    }

    @Test
    void delete_Success() {
        // Arrange
        Long lessonId = 1L;

        Mockito.when(lessonRepository.existsById(lessonId)).thenReturn(true);

        // Act
        lessonService.delete(lessonId);

        // Assert
        Mockito.verify(lessonRepository, Mockito.times(1)).existsById(lessonId);
        Mockito.verify(lessonRepository, Mockito.times(1)).deleteById(lessonId);
    }

    @Test
    void getByChapterId_Success() {
        // Arrange
        Long chapterId = 1L;
        Chapter fakeChapter = new Chapter(1L, "Переменные", "Описание", 1, null, LocalDateTime.now(), LocalDateTime.now());
        Lesson lesson1 = new Lesson(1L, "Урок 1", "Опис 1", "Текст 1", 1, fakeChapter, LocalDateTime.now(), LocalDateTime.now());
        Lesson lesson2 = new Lesson(2L, "Урок 2", "Опис 2", "Текст 2", 2, fakeChapter, LocalDateTime.now(), LocalDateTime.now());

        Mockito.when(chapterRepository.existsById(chapterId)).thenReturn(false);
        Mockito.when(lessonRepository.findByChapterIdOrderByLessonOrderAsc(chapterId)).thenReturn(List.of(lesson1, lesson2));

        // Act
        List<LessonResponseDto> result = lessonService.getByChapterId(chapterId);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals("Урок 1", result.get(0).name());
        Assertions.assertEquals("Урок 2", result.get(1).name());

        Mockito.verify(chapterRepository, Mockito.times(1)).existsById(chapterId);
        Mockito.verify(lessonRepository, Mockito.times(1)).findByChapterIdOrderByLessonOrderAsc(chapterId);
    }
}