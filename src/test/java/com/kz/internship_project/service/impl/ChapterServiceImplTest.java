package com.kz.internship_project.service.impl;

import com.kz.internship_project.dto.chapter.ChapterCreateDto;
import com.kz.internship_project.dto.chapter.ChapterResponseDto;
import com.kz.internship_project.entity.Chapter;
import com.kz.internship_project.entity.Course;
import com.kz.internship_project.entity.Lesson;
import com.kz.internship_project.mapper.ChapterMapper;
import com.kz.internship_project.repository.ChapterRepository;
import com.kz.internship_project.repository.CourseRepository;
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
class ChapterServiceImplTest {

    @Mock
    private ChapterRepository chapterRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private LessonRepository lessonRepository;

    @InjectMocks
    private ChapterServiceImpl chapterService;

    @Spy
    private ChapterMapper chapterMapper = Mappers.getMapper(ChapterMapper.class);

    private Course fakeCourse;
    private Chapter fakeChapter;
    private Lesson fakeLesson;
    private ChapterCreateDto createDto;

    @BeforeEach
    void setUp(){
        LocalDateTime now = LocalDateTime.now();

        fakeCourse = new Course(1L, "Java Core", "Все про Java", now, now);

        fakeChapter = new Chapter(1L, "Переменные", "Строки, числовые, логические переменные", 1, fakeCourse, now, now);

        fakeLesson = new Lesson(1L, "Строки", "Все о строках", "Большой урок о строках", 1, fakeChapter, now, now);

        createDto = new ChapterCreateDto("Переменные", "Строки, числовые, логические переменные", 1L);
    }

    //----------------------------------
    //Позитивные сценарии
    //----------------------------------

    @Test
    void create_Success() {
        //Arrange

        Chapter savedChapter = fakeChapter;

        Mockito.when(courseRepository.findById(createDto.courseId())).thenReturn(Optional.of(fakeCourse));

        Mockito.when(chapterRepository.save(Mockito.any(Chapter.class))).thenReturn(savedChapter);

        Mockito.when(chapterRepository.findFirstByCourseIdOrderByChapterOrderDesc(createDto.courseId())).thenReturn(Optional.empty());
        //Act
        ChapterResponseDto result = chapterService.create(createDto);

        //Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1L, result.id());
        Assertions.assertEquals("Переменные", result.name());
        Assertions.assertEquals("Строки, числовые, логические переменные", result.description());
        Assertions.assertEquals(1, result.chapterOrder());
        Assertions.assertEquals(fakeCourse.getId(), result.courseId());

        Mockito.verify(courseRepository, Mockito.times(1)).findById(createDto.courseId());
        Mockito.verify(chapterRepository, Mockito.times(1)).save(Mockito.any(Chapter.class));
        Mockito.verify(chapterRepository, Mockito.times(1)).findFirstByCourseIdOrderByChapterOrderDesc(createDto.courseId());
    }

    @Test
    void getById_Success() {
        //Arrange
        Mockito.when(chapterRepository.findById(fakeChapter.getId())).thenReturn(Optional.of(fakeChapter));

        //Act
        ChapterResponseDto testChapter = chapterService.getById(fakeChapter.getId());

        //Assert
        Assertions.assertEquals(fakeChapter.getId(), testChapter.id());
        Assertions.assertEquals(fakeChapter.getName(), testChapter.name());

        Mockito.verify(chapterRepository, Mockito.times(1)).findById(fakeChapter.getId());
    }

    @Test
    void update_Success() {
        //Arrange
        ChapterCreateDto updateDto = new ChapterCreateDto("Generics", "Как работают дженерики", fakeCourse.getId());

        Mockito.when(courseRepository.findById(fakeCourse.getId())).thenReturn(Optional.of(fakeCourse));
        Mockito.when(chapterRepository.findById(fakeChapter.getId())).thenReturn(Optional.of(fakeChapter));
        Mockito.when(chapterRepository.save(Mockito.any(Chapter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //Act
        ChapterResponseDto updatedChapter = chapterService.update(fakeChapter.getId(), updateDto);

        //Assert
        Assertions.assertNotNull(updatedChapter);
        Assertions.assertEquals(updateDto.courseId(), updatedChapter.courseId());
        Assertions.assertEquals(updateDto.name(), updatedChapter.name());
        Assertions.assertEquals(updateDto.description(), updatedChapter.description());

        Mockito.verify(courseRepository, Mockito.times(1)).findById(updateDto.courseId());
        Mockito.verify(chapterRepository, Mockito.times(1)).findById(fakeChapter.getId());
        Mockito.verify(chapterRepository, Mockito.times(1)).save(fakeChapter);

    }

    @Test
    void delete_Success() {
        //Arrange
        Mockito.when(chapterRepository.existsById(fakeChapter.getId())).thenReturn(true);
        Mockito.when(lessonRepository.existsById(fakeLesson.getId())).thenReturn(false);

        //Act
        chapterService.delete(fakeChapter.getId());
        //Assert
        Mockito.verify(chapterRepository, Mockito.times(1)).existsById(fakeChapter.getId());
        Mockito.verify(lessonRepository, Mockito.times(1)).existsById(fakeLesson.getId());
        Mockito.verify(chapterRepository, Mockito.times(1)).deleteById(fakeChapter.getId());

    }

    @Test
    void getByCourseId_Success() {

        //Arrange
        Chapter fakeChapter2 = new Chapter(2L, "Циклы", "Описание 2", 2, fakeCourse, LocalDateTime.now(), LocalDateTime.now());
        List<Chapter> fakeChapters = List.of(fakeChapter, fakeChapter2);

        Mockito.when(courseRepository.existsById(fakeCourse.getId())).thenReturn(true);
        Mockito.when(chapterRepository.findByCourseIdOrderByChapterOrderAsc(fakeCourse.getId())).thenReturn(fakeChapters);

        //Act
        List<ChapterResponseDto> result = chapterService.getByCourseId(fakeCourse.getId());
        //Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());

        Assertions.assertEquals(fakeChapter.getName(), result.get(0).name());
        Assertions.assertEquals(fakeChapter2.getName(), result.get(1).name());

        Mockito.verify(courseRepository, Mockito.times(1)).existsById(fakeCourse.getId());
        Mockito.verify(chapterRepository, Mockito.times(1)).findByCourseIdOrderByChapterOrderAsc(fakeCourse.getId());
    }

    //----------------------------------
    //Негативные сценарии
    //----------------------------------

    @Test
    void create_CourseNotFound_ThrowsEntityNotFoundException() {
        //Arrange

        Mockito.when(courseRepository.findById(fakeCourse.getId())).thenReturn(Optional.empty());

        //Act
        EntityNotFoundException exception = Assertions.assertThrows(EntityNotFoundException.class, () -> chapterService.create(createDto));

        //Assert
        Assertions.assertEquals("Курс с id " + createDto.courseId() + " не найден", exception.getMessage());
        Mockito.verify(courseRepository, Mockito.times(1)).findById(fakeCourse.getId());

    }


    @Test
    void getById_ChapterNotFound_ThrowsEntityNotFoundException() {
        //Arrange
        Mockito.when(chapterRepository.findById(fakeChapter.getId())).thenReturn(Optional.empty());

        //Act
        EntityNotFoundException exception = Assertions.assertThrows(EntityNotFoundException.class, () -> chapterService.getById(fakeChapter.getId()));

        //Assert
        Assertions.assertEquals("Глава с id " + fakeChapter.getId() + " не найдена", exception.getMessage());

        Mockito.verify(chapterRepository, Mockito.times(1)).findById(fakeChapter.getId());
    }


    @Test
    void update_CourseNotFoundException_EntityNotFoundException() {

        //Arrange
        Long courseId = 1L;
        Mockito.when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        //Act
        EntityNotFoundException exception = Assertions.assertThrows(EntityNotFoundException.class, () -> chapterService.update(courseId, createDto));

        //Assert
        Assertions.assertEquals("Курс с id " + createDto.courseId() + " не найден", exception.getMessage());
        Mockito.verify(courseRepository, Mockito.times(1)).findById(Mockito.any());
    }

    @Test
    void update_ChapterNotFound_ThrowsEntityNotFoundException() {
        //Arrange
        Long chapterId = 1L;
        Long courseId = 10L;

        Mockito.when(courseRepository.findById(courseId)).thenReturn(Optional.of(fakeCourse));
        Mockito.when(chapterRepository.findById(chapterId)).thenReturn(Optional.empty());

        //Act
        EntityNotFoundException exception = Assertions.assertThrows(EntityNotFoundException.class, () -> chapterService.update(chapterId, createDto));
        //Assert
        Assertions.assertEquals("Глава с id " + chapterId + " не найдена", exception.getMessage());

        Mockito.verify(courseRepository, Mockito.times(1)).findById(Mockito.any());
        Mockito.verify(chapterRepository, Mockito.times(1)).findById(Mockito.any());
        Mockito.verify(chapterRepository, Mockito.never()).save(Mockito.any());
    }


    @Test
    void delete_ChapterNotFound_ThrowsEntityNotFoundException() {
        //Arrange
        Long chapterId = 1L;
        Mockito.when(chapterRepository.existsById(chapterId)).thenReturn(false);
        //Act
        EntityNotFoundException exception = Assertions.assertThrows(EntityNotFoundException.class, () -> chapterService.delete(chapterId));
        //Assert
        Assertions.assertEquals("Глава с id " + chapterId + " не найдена", exception.getMessage());

        Mockito.verify(chapterRepository, Mockito.never()).deleteById(Mockito.any());
    }

    @Test
    void delete_HasLessons_ThrowsIllegalArgumentException() {
        //Arrange
        Long chapterId = 1L;

        Mockito.when(chapterRepository.existsById(chapterId)).thenReturn(true);
        Mockito.when(lessonRepository.existsById(chapterId)).thenReturn(true);

        //Act
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> chapterService.delete(chapterId));
        //Assert
        Assertions.assertEquals("Главу нельзя удалить, пока в ней есть уроки!", exception.getMessage());

        Mockito.verify(lessonRepository, Mockito.times(1)).existsById(chapterId);
        Mockito.verify(chapterRepository, Mockito.never()).deleteById(Mockito.any());

    }


    @Test
    void getByCourseId_CourseNotFound_ThrowsEntityNotFoundException() {
        //Arrange
        Long courseId = 1L;

        Mockito.when(courseRepository.existsById(courseId)).thenReturn(false);
        //Act
        EntityNotFoundException exception = Assertions.assertThrows(EntityNotFoundException.class, () -> chapterService.getByCourseId(courseId));
        //Assert
        Assertions.assertEquals("Курс с id " + courseId + " не найден", exception.getMessage());

        Mockito.verify(courseRepository, Mockito.times(1)).existsById(courseId);
        Mockito.verify(chapterRepository, Mockito.never()).findByCourseIdOrderByChapterOrderAsc(Mockito.any());

    }
}