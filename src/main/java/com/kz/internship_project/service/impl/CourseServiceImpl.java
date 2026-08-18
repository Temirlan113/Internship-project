package com.kz.internship_project.service.impl;

import com.kz.internship_project.dto.CourseCreateDto;
import com.kz.internship_project.dto.CourseResponseDto;
import com.kz.internship_project.entity.Course;
import com.kz.internship_project.mapper.CourseMapper;
import com.kz.internship_project.repository.ChapterRepository;
import com.kz.internship_project.repository.CourseRepository;
import com.kz.internship_project.service.CourseService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final ChapterRepository chapterRepository;

    @Override
    @Transactional
    public CourseResponseDto create(CourseCreateDto dto) {

        log.info("Создание нового курса");
        log.debug("Создание курса с данными: {}", dto);

        Course course = courseMapper.toEntity(dto);
        Course savedCourse = courseRepository.save(course);
        CourseResponseDto courseResponseDto = courseMapper.toDto(savedCourse);

        log.debug("Курс успешно сохранен: {}", courseResponseDto);
        return courseResponseDto;
    }

    @Override
    public CourseResponseDto getById(Long id) {
        log.info("Получение курса по id: {}", id);

        Course course = courseRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Курс с id " + id + " не найден"));
        return courseMapper.toDto(course);
    }

    @Override
    @Transactional
    public CourseResponseDto update(Long id, CourseCreateDto dto) {
        log.info("Обновление курса по id: {}", id);
        log.debug("Обновление курса с данными: {}", dto);

        Course existingCourse = courseRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Курс с id " + id + " не найден"));
        existingCourse.setName(dto.name());
        existingCourse.setDescription(dto.description());
        Course updatedCourse = courseRepository.save(existingCourse);

        CourseResponseDto courseResponseDto = courseMapper.toDto(updatedCourse);

        log.debug("Успешное обновление курса: {}", courseResponseDto);
        return courseResponseDto;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Удаление курса по id: {}", id);

        if (!courseRepository.existsById(id)) {
            throw new EntityNotFoundException("Курс с id " + id + " не найден");
        }

        if (chapterRepository.existsById(id)){
            throw new IllegalArgumentException("Курс нельзя удалить, пока в нем есть главы!");
        }
        courseRepository.deleteById(id);
    }

    @Override
    public Page<CourseResponseDto> getAll(int page, int size, String sortBy, String sortDir){

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.DESC.name())
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Course> courses =courseRepository.findAll(pageable);

        return courses.map(courseMapper::toDto);
    }

}
