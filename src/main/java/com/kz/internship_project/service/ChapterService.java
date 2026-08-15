package com.kz.internship_project.service;

import com.kz.internship_project.dto.ChapterCreateDto;
import com.kz.internship_project.dto.ChapterResponseDto;
import com.kz.internship_project.entity.Chapter;
import com.kz.internship_project.repository.ChapterRepository;

import java.util.List;

public interface ChapterService {

    ChapterResponseDto create (ChapterCreateDto dto);

    ChapterResponseDto getById(Long id);

    ChapterResponseDto update(Long id, ChapterCreateDto dto);

    void delete(Long id);

    List<ChapterResponseDto> getByCourseId(Long courseId);


}
