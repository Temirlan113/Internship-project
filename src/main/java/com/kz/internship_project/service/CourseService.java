package com.kz.internship_project.service;

import com.kz.internship_project.dto.CourseCreateDto;
import com.kz.internship_project.dto.CourseResponseDto;

public interface CourseService {

    CourseResponseDto create (CourseCreateDto dto);

    CourseResponseDto getById(Long id);

    CourseResponseDto update(Long id, CourseCreateDto dto);

    void delete(Long id);
}
