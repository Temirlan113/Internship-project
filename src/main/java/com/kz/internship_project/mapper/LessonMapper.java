package com.kz.internship_project.mapper;

import com.kz.internship_project.dto.LessonCreateDto;
import com.kz.internship_project.dto.LessonResponseDto;
import com.kz.internship_project.entity.Lesson;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LessonMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lessonOrder", ignore = true)
    @Mapping(target = "chapter", ignore = true)
    @Mapping(target = "createdTime", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedTime", expression = "java(java.time.LocalDateTime.now())")
    Lesson toEntity(LessonCreateDto dto);

    @Mapping(source = "chapter.id", target = "chapterId")
    LessonResponseDto toDto(Lesson entity);
}