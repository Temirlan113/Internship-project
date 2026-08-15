package com.kz.internship_project.mapper;

import com.kz.internship_project.dto.ChapterCreateDto;
import com.kz.internship_project.dto.ChapterResponseDto;
import com.kz.internship_project.entity.Chapter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChapterMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "chapterOrder", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "createdTime", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedTime", expression = "java(java.time.LocalDateTime.now())")
    Chapter toEntity(ChapterCreateDto dto);

    @Mapping(source = "course.id", target = "courseId")
    ChapterResponseDto toDto(Chapter entity);
}
