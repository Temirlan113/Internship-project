package com.kz.internship_project.service;

import com.kz.internship_project.dto.course.CourseCreateDto;
import com.kz.internship_project.dto.course.CourseResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CourseService {

    CourseResponseDto create (CourseCreateDto dto);

    CourseResponseDto getById(Long id);

    CourseResponseDto update(Long id, CourseCreateDto dto);

    void delete(Long id);

    Page<CourseResponseDto> getAll(Pageable pageable);
}
