package com.kz.internship_project.mapper;

import com.kz.internship_project.dto.course.CourseCreateDto;
import com.kz.internship_project.dto.course.CourseResponseDto;
import com.kz.internship_project.entity.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface CourseMapper {

    @Mapping(target = "id", ignore = true)

    Course toEntity(CourseCreateDto dto);

    CourseResponseDto toDto(Course entity);
}
