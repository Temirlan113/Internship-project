package com.kz.internship_project.service.impl;

import com.kz.internship_project.dto.lesson.LessonCreateDto;
import com.kz.internship_project.dto.lesson.LessonResponseDto;
import com.kz.internship_project.entity.Chapter;
import com.kz.internship_project.entity.Lesson;
import com.kz.internship_project.mapper.LessonMapper;
import com.kz.internship_project.repository.ChapterRepository;
import com.kz.internship_project.repository.LessonRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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

    private Chapter fakeChapter;
    private Lesson fakeLesson;
    private LessonCreateDto createDto;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        fakeChapter = new Chapter(1L, "Переменные", "Описание главы", 1, null, now, now);
        fakeLesson = new Lesson(1L, "Строки", "Урок про строки", "Текст урока...", 1, fakeChapter, now, now);
        createDto = new LessonCreateDto("Строки", "Урок про строки", "Текст урока...", 1L);
    }


    //----------------------------------
    //Позитивные сценарии
    //----------------------------------


    @Test
    void create_Success() {
        // Arrange
        Mockito.when(chapterRepository.findById(createDto.chapterId())).thenReturn(Optional.of(fakeChapter));
        Mockito.when(lessonRepository.findFirstByChapterIdOrderByLessonOrderDesc(createDto.chapterId())).thenReturn(Optional.empty());
        Mockito.when(lessonRepository.save(Mockito.any(Lesson.class))).thenReturn(fakeLesson);

        // Act
        LessonResponseDto result = lessonService.create(createDto);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(fakeLesson.getId(), result.id());
        Assertions.assertEquals(fakeLesson.getName(), result.name());
        Assertions.assertEquals(fakeLesson.getDescription(), result.description());
        Assertions.assertEquals(fakeLesson.getContent(), result.content());

        Mockito.verify(chapterRepository, Mockito.times(1)).findById(createDto.chapterId());
        Mockito.verify(lessonRepository, Mockito.times(1)).findFirstByChapterIdOrderByLessonOrderDesc(createDto.chapterId());
        Mockito.verify(lessonRepository, Mockito.times(1)).save(Mockito.any(Lesson.class));
    }

    @Test
    void getById_Success() {
        // Arrange
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
        LessonCreateDto updateDto = new LessonCreateDto("Числа", "Урок про числа", "Новый текст...", fakeChapter.getId());

        Mockito.when(chapterRepository.findById(updateDto.chapterId())).thenReturn(Optional.of(fakeChapter));
        Mockito.when(lessonRepository.findById(fakeLesson.getId())).thenReturn(Optional.of(fakeLesson));
        Mockito.when(lessonRepository.save(Mockito.any(Lesson.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        LessonResponseDto result = lessonService.update(fakeLesson.getId(), updateDto);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(updateDto.name(), result.name());
        Assertions.assertEquals(updateDto.description(), result.description());
        Assertions.assertEquals(updateDto.content(), result.content());

        Mockito.verify(chapterRepository, Mockito.times(1)).findById(updateDto.chapterId());
        Mockito.verify(lessonRepository, Mockito.times(1)).findById(fakeLesson.getId());
        Mockito.verify(lessonRepository, Mockito.times(1)).save(fakeLesson);
    }

    @Test
    void delete_Success() {
        // Arrange
        Long lessonId = fakeLesson.getId();
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
        Long chapterId = fakeChapter.getId();
        Lesson lesson2 = new Lesson(2L, "Урок 2", "Опис 2", "Текст 2", 2, fakeChapter, LocalDateTime.now(), LocalDateTime.now());

        // Исправлено: должно быть true, чтобы пройти проверку в сервисе
        Mockito.when(chapterRepository.existsById(chapterId)).thenReturn(true);
        Mockito.when(lessonRepository.findByChapterIdOrderByLessonOrderAsc(chapterId)).thenReturn(List.of(fakeLesson, lesson2));

        // Act
        List<LessonResponseDto> result = lessonService.getByChapterId(chapterId);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(fakeLesson.getName(), result.get(0).name());
        Assertions.assertEquals(lesson2.getName(), result.get(1).name());

        Mockito.verify(chapterRepository, Mockito.times(1)).existsById(chapterId);
        Mockito.verify(lessonRepository, Mockito.times(1)).findByChapterIdOrderByLessonOrderAsc(chapterId);
    }


    //----------------------------------
    //Негативные
    //----------------------------------


    @Test
    void create_ChapterNotFound_ThrowsEntityNotFoundException() {
        // Arrange
        Mockito.when(chapterRepository.findById(createDto.chapterId())).thenReturn(Optional.empty());

        // Act
        EntityNotFoundException exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> lessonService.create(createDto)
        );

        // Assert
        Assertions.assertEquals("Глава с id " + createDto.chapterId() + " не найдена", exception.getMessage());
        Mockito.verify(chapterRepository, Mockito.times(1)).findById(createDto.chapterId());
        Mockito.verify(lessonRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void getById_NotFound_ThrowsEntityNotFoundException() {
        // Arrange
        Long nonExistentId = 99L;
        Mockito.when(lessonRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act
        EntityNotFoundException exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> lessonService.getById(nonExistentId)
        );

        // Assert
        Assertions.assertEquals("Урок с id " + nonExistentId + " не найден", exception.getMessage());
        Mockito.verify(lessonRepository, Mockito.times(1)).findById(nonExistentId);
    }

    @Test
    void update_ChapterNotFound_ThrowsEntityNotFoundException() {
        // Arrange
        Long lessonId = fakeLesson.getId();
        LessonCreateDto updateDto = new LessonCreateDto("Числа", "Урок про числа", "Текст...", 99L);

        Mockito.when(chapterRepository.findById(updateDto.chapterId())).thenReturn(Optional.empty());

        // Act
        EntityNotFoundException exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> lessonService.update(lessonId, updateDto)
        );

        // Assert
        Assertions.assertEquals("Глава с id " + updateDto.chapterId() + " не найдена", exception.getMessage());
        Mockito.verify(chapterRepository, Mockito.times(1)).findById(updateDto.chapterId());
        Mockito.verify(lessonRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void update_LessonNotFound_ThrowsEntityNotFoundException() {
        // Arrange
        Long nonExistentLessonId = 99L;

        Mockito.when(chapterRepository.findById(createDto.chapterId())).thenReturn(Optional.of(fakeChapter));
        Mockito.when(lessonRepository.findById(nonExistentLessonId)).thenReturn(Optional.empty());

        // Act
        EntityNotFoundException exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> lessonService.update(nonExistentLessonId, createDto)
        );

        // Assert
        Assertions.assertEquals("Урок с id " + nonExistentLessonId + " не найден", exception.getMessage());
        Mockito.verify(chapterRepository, Mockito.times(1)).findById(createDto.chapterId());
        Mockito.verify(lessonRepository, Mockito.times(1)).findById(nonExistentLessonId);
        Mockito.verify(lessonRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void delete_LessonNotFound_ThrowsEntityNotFoundException() {
        // Arrange
        Long nonExistentId = 99L;
        Mockito.when(lessonRepository.existsById(nonExistentId)).thenReturn(false);

        // Act
        EntityNotFoundException exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> lessonService.delete(nonExistentId)
        );

        // Assert
        Assertions.assertEquals("Урок с id " + nonExistentId + " не найден", exception.getMessage());
        Mockito.verify(lessonRepository, Mockito.times(1)).existsById(nonExistentId);
        Mockito.verify(lessonRepository, Mockito.never()).deleteById(Mockito.any());
    }

    @Test
    void getByChapterId_ChapterNotFound_ThrowsEntityNotFoundException() {
        // Arrange
        Long nonExistentChapterId = 99L;
        Mockito.when(chapterRepository.existsById(nonExistentChapterId)).thenReturn(false);

        // Act
        EntityNotFoundException exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> lessonService.getByChapterId(nonExistentChapterId)
        );

        // Assert
        Assertions.assertEquals("Глава с id " + nonExistentChapterId + " не найдена", exception.getMessage());
        Mockito.verify(chapterRepository, Mockito.times(1)).existsById(nonExistentChapterId);
        Mockito.verify(lessonRepository, Mockito.never()).findByChapterIdOrderByLessonOrderAsc(Mockito.any());
    }
}