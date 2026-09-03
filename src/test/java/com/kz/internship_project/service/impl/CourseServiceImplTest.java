package com.kz.internship_project.service.impl;

import com.kz.internship_project.dto.course.CourseCreateDto;
import com.kz.internship_project.dto.course.CourseResponseDto;
import com.kz.internship_project.entity.Course;
import com.kz.internship_project.mapper.CourseMapper;
import com.kz.internship_project.repository.ChapterRepository;
import com.kz.internship_project.repository.CourseRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class CourseServiceImplTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private ChapterRepository chapterRepository;

    @Spy
    private CourseMapper courseMapper = Mappers.getMapper(CourseMapper.class);

    @InjectMocks
    private CourseServiceImpl courseService;

    private Course fakeCourse;
    private CourseCreateDto createDto;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        fakeCourse = new Course(1L, "Java Core", "Все про Java", now, now);
        createDto = new CourseCreateDto("Java Core", "Все про Java");
    }

    //----------------------------------
    //Позитивные сценарии
    //----------------------------------

    @Test
    void create_Success() {
        // Arrange
        Mockito.when(courseRepository.save(Mockito.any(Course.class))).thenReturn(fakeCourse);

        // Act
        CourseResponseDto result = courseService.create(createDto);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(fakeCourse.getId(), result.id());
        Assertions.assertEquals(fakeCourse.getName(), result.name());
        Assertions.assertEquals(fakeCourse.getDescription(), result.description());

        Mockito.verify(courseRepository, Mockito.times(1)).save(Mockito.any(Course.class));
    }

    @Test
    void getById_Success() {
        // Arrange
        Mockito.when(courseRepository.findById(fakeCourse.getId())).thenReturn(Optional.of(fakeCourse));

        // Act
        CourseResponseDto result = courseService.getById(fakeCourse.getId());

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(fakeCourse.getId(), result.id());
        Assertions.assertEquals(fakeCourse.getName(), result.name());
        Assertions.assertEquals(fakeCourse.getDescription(), result.description());

        Mockito.verify(courseRepository, Mockito.times(1)).findById(fakeCourse.getId());
    }

    @Test
    void update_Success() {
        // Arrange
        CourseCreateDto updateDto = new CourseCreateDto("Spring Boot", "Продвинутый курс");

        Mockito.when(courseRepository.findById(fakeCourse.getId())).thenReturn(Optional.of(fakeCourse));
        Mockito.when(courseRepository.save(Mockito.any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CourseResponseDto updatedCourse = courseService.update(fakeCourse.getId(), updateDto);

        // Assert
        Assertions.assertNotNull(updatedCourse);
        Assertions.assertEquals(updateDto.name(), updatedCourse.name());
        Assertions.assertEquals(updateDto.description(), updatedCourse.description());

        Mockito.verify(courseRepository, Mockito.times(1)).findById(fakeCourse.getId());
        Mockito.verify(courseRepository, Mockito.times(1)).save(fakeCourse);
    }

    @Test
    void delete_Success() {
        // Arrange
        Long courseId = fakeCourse.getId();

        Mockito.when(courseRepository.existsById(courseId)).thenReturn(true);
        Mockito.when(chapterRepository.existsByCourseId(courseId)).thenReturn(false);

        // Act
        courseService.delete(courseId);

        // Assert
        Mockito.verify(courseRepository, Mockito.times(1)).existsById(courseId);
        Mockito.verify(chapterRepository, Mockito.times(1)).existsByCourseId(courseId);
        Mockito.verify(courseRepository, Mockito.times(1)).deleteById(courseId);
    }

    @Test
    void getAll_Success() {
        // Arrange
        Course fakeCourse2 = new Course(2L, "Spring Core", "Все про Spring", LocalDateTime.now(), LocalDateTime.now());
        List<Course> coursesList = List.of(fakeCourse, fakeCourse2);
        Page<Course> coursePage = new PageImpl<>(coursesList);

        Mockito.when(courseRepository.findAll(Mockito.any(Pageable.class))).thenReturn(coursePage);

        // Act
        Page<CourseResponseDto> result = courseService.getAll(0, 10, "id", "asc");

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.getTotalElements());
        Assertions.assertEquals("Java Core", result.getContent().get(0).name());
        Assertions.assertEquals("Spring Core", result.getContent().get(1).name());

        Mockito.verify(courseRepository, Mockito.times(1)).findAll(Mockito.any(Pageable.class));
    }

    //----------------------------------
    //Негативные сценарии
    //----------------------------------

    @Test
    void getById_NotFound_ThrowsEntityNotFoundException() {
        // Arrange
        Long nonExistentId = 99L;
        Mockito.when(courseRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act
        EntityNotFoundException exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> courseService.getById(nonExistentId)
        );

        // Assert
        Assertions.assertEquals("Курс с id " + nonExistentId + " не найден", exception.getMessage());
        Mockito.verify(courseRepository, Mockito.times(1)).findById(nonExistentId);
    }

    @Test
    void update_NotFound_ThrowsEntityNotFoundException() {
        // Arrange
        Long nonExistentId = 99L;
        CourseCreateDto updateDto = new CourseCreateDto("Spring Boot", "Продвинутый курс");

        Mockito.when(courseRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act
        EntityNotFoundException exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> courseService.update(nonExistentId, updateDto)
        );

        // Assert
        Assertions.assertEquals("Курс с id " + nonExistentId + " не найден", exception.getMessage());
        Mockito.verify(courseRepository, Mockito.times(1)).findById(nonExistentId);
        Mockito.verify(courseRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void delete_CourseNotFound_ThrowsEntityNotFoundException() {
        // Arrange
        Long nonExistentId = 99L;
        Mockito.when(courseRepository.existsById(nonExistentId)).thenReturn(false);

        // Act
        EntityNotFoundException exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> courseService.delete(nonExistentId)
        );

        // Assert
        Assertions.assertEquals("Курс с id " + nonExistentId + " не найден", exception.getMessage());
        Mockito.verify(courseRepository, Mockito.times(1)).existsById(nonExistentId);
        Mockito.verify(chapterRepository, Mockito.never()).existsByCourseId(Mockito.any());
        Mockito.verify(courseRepository, Mockito.never()).deleteById(Mockito.any());
    }

    @Test
    void delete_HasChapters_ThrowsIllegalArgumentException() {
        // Arrange
        Long courseId = fakeCourse.getId();

        Mockito.when(courseRepository.existsById(courseId)).thenReturn(true);
        Mockito.when(chapterRepository.existsByCourseId(courseId)).thenReturn(true);

        // Act
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> courseService.delete(courseId)
        );

        // Assert
        Assertions.assertEquals("Курс нельзя удалить, пока в нем есть главы!", exception.getMessage());
        Mockito.verify(courseRepository, Mockito.times(1)).existsById(courseId);
        Mockito.verify(chapterRepository, Mockito.times(1)).existsByCourseId(courseId);
        Mockito.verify(courseRepository, Mockito.never()).deleteById(Mockito.any());
    }
}