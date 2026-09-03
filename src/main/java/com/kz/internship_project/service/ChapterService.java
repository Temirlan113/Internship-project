package com.kz.internship_project.service;

import com.kz.internship_project.dto.chapter.ChapterCreateDto;
import com.kz.internship_project.dto.chapter.ChapterResponseDto;

import java.util.List;

public interface ChapterService {

    ChapterResponseDto create (ChapterCreateDto dto);

    ChapterResponseDto getById(Long id);

    ChapterResponseDto update(Long id, ChapterCreateDto dto);

    void delete(Long id);

    List<ChapterResponseDto> getByCourseId(Long courseId);




}
