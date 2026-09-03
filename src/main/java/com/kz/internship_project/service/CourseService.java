package com.kz.internship_project.service;

import com.kz.internship_project.dto.course.CourseCreateDto;
import com.kz.internship_project.dto.course.CourseResponseDto;
import org.springframework.data.domain.Page;

public interface CourseService {

    CourseResponseDto create (CourseCreateDto dto);

    CourseResponseDto getById(Long id);

    CourseResponseDto update(Long id, CourseCreateDto dto);

    void delete(Long id);

    Page<CourseResponseDto> getAll(int page, int size, String sortBy, String sortDir);
}
