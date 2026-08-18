package com.kz.internship_project.service;

import com.kz.internship_project.dto.CourseResponseDto;
import com.kz.internship_project.dto.LessonCreateDto;
import com.kz.internship_project.dto.LessonResponseDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface LessonService {

    LessonResponseDto create(LessonCreateDto dto);

    LessonResponseDto getById(Long id);

    LessonResponseDto update(Long id, LessonCreateDto dto);

    void delete(Long id);

    List<LessonResponseDto> getByChapterId(Long chapterId);



}
