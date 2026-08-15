package com.kz.internship_project.mapper;

import com.kz.internship_project.dto.CourseCreateDto;
import com.kz.internship_project.dto.CourseResponseDto;
import com.kz.internship_project.entity.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface CourseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdTime", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedTime", expression = "java(java.time.LocalDateTime.now())")

    Course toEntity(CourseCreateDto dto);

    CourseResponseDto toDto(Course entity);
}
