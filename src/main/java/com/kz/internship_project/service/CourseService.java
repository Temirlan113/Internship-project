package com.kz.internship_project.service;

import com.kz.internship_project.dto.CourseCreateDto;
import com.kz.internship_project.dto.CourseResponseDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CourseService {

    CourseResponseDto create (CourseCreateDto dto);

    CourseResponseDto getById(Long id);

    CourseResponseDto update(Long id, CourseCreateDto dto);

    void delete(Long id);

    Page<CourseResponseDto> getAll(int page, int size, String sortBy, String sortDir);
}
