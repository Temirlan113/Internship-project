package com.kz.internship_project.service;

import com.kz.internship_project.dto.lesson.LessonCreateDto;
import com.kz.internship_project.dto.lesson.LessonResponseDto;

import java.util.List;

public interface LessonService {

    LessonResponseDto create(LessonCreateDto dto);

    LessonResponseDto getById(Long id);

    LessonResponseDto update(Long id, LessonCreateDto dto);

    void delete(Long id);

    List<LessonResponseDto> getByChapterId(Long chapterId);




}
