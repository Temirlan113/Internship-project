package com.kz.internship_project.service.impl;

import com.kz.internship_project.dto.CourseCreateDto;
import com.kz.internship_project.dto.CourseResponseDto;
import com.kz.internship_project.entity.Course;
import com.kz.internship_project.mapper.CourseMapper;
import com.kz.internship_project.repository.ChapterRepository;
import com.kz.internship_project.repository.CourseRepository;
import org.junit.jupiter.api.Assertions;
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

    @Test
    void create_Success() {
                //Arrange
        CourseCreateDto createDto = new CourseCreateDto("Java Core", "Все про Java");
        Course savedCourse = new Course(1L, "Java Core", "Все про Java", LocalDateTime.now(), LocalDateTime.now());


        Mockito.when(courseRepository.save(Mockito.any(Course.class))).thenReturn(savedCourse);

        //Act

        CourseResponseDto result = courseService.create(createDto);

        //Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1L, result.id());
        Assertions.assertEquals("Java Core", result.name());
        Assertions.assertEquals("Все про Java", result.description());

        Mockito.verify(courseRepository, Mockito.times(1)).save(Mockito.any(Course.class));
    }

    @Test
    void getById_Success() {
        //Arrange
        Course fakeCourse = new Course(1L, "Java Core", "Все про Java", LocalDateTime.now(), LocalDateTime.now());

        Mockito.when(courseRepository.findById(fakeCourse.getId())).thenReturn(Optional.of(fakeCourse));

        //Act
        CourseResponseDto result = courseService.getById(fakeCourse.getId());

                //Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(fakeCourse.getId(), result.id());
        Assertions.assertEquals(fakeCourse.getName(), result.name());
        Assertions.assertEquals(fakeCourse.getDescription(), result.description());

        Mockito.verify(courseRepository, Mockito.times(1)).findById(fakeCourse.getId());
    }

    @Test
    void update_Success() {
  //Arrange
        Course oldCourse = new Course(1L, "Java Core", "Все про Java", LocalDateTime.now(), LocalDateTime.now());
        CourseCreateDto updateDto = new CourseCreateDto("Spring Boot", "Продвинутый курс");

        Mockito.when(courseRepository.findById(oldCourse.getId())).thenReturn(Optional.of(oldCourse));
        Mockito.when(courseRepository.save(Mockito.any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //Act
        CourseResponseDto updatedCourse = courseService.update(oldCourse.getId(), updateDto);

        //Assert

        Assertions.assertNotNull(updatedCourse);
        Assertions.assertEquals(updateDto.name(), updatedCourse.name());
        Assertions.assertEquals(updateDto.description(), updatedCourse.description());

        Mockito.verify(courseRepository, Mockito.times(1)).findById(oldCourse.getId());
        Mockito.verify(courseRepository, Mockito.times(1)).save(oldCourse);
    }

    @Test
    void delete_Success() {
  //Arrange
        Long courseId = 1L;

        Mockito.when(courseRepository.existsById(courseId)).thenReturn(true);
        Mockito.when(chapterRepository.existsById(courseId)).thenReturn(false);

        //Act
        courseService.delete(courseId);

        //Assert
        Mockito.verify(courseRepository, Mockito.times(1)).existsById(courseId);
        Mockito.verify(chapterRepository, Mockito.times(1)).existsById(courseId);
        Mockito.verify(courseRepository, Mockito.times(1)).deleteById(courseId);
    }

    @Test
    void getAll_Success() {
        //Arrange

        Course fakeCourse1 = new Course(1L, "Java Core", "Все про Java", LocalDateTime.now(), LocalDateTime.now());
        Course fakeCourse2 = new Course(2L, "Spring Core", "Все про Spring", LocalDateTime.now(), LocalDateTime.now());
        List<Course> coursesList = List.of(fakeCourse1, fakeCourse2);
        Page<Course> coursePage = new PageImpl<>(coursesList);

        Mockito.when(courseRepository.findAll(Mockito.any(Pageable.class))).thenReturn(coursePage);
        //Act
        Page<CourseResponseDto> result = courseService.getAll(0, 10, "id", "asc");

        //Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.getTotalElements());
        Assertions.assertEquals("Java Core", result.getContent().get(0).name());
        Assertions.assertEquals("Spring Core", result.getContent().get(1).name());

        Mockito.verify(courseRepository, Mockito.times(1)).findAll(Mockito.any(Pageable.class));
    }
}