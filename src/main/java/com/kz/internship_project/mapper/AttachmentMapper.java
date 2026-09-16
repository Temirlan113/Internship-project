package com.kz.internship_project.mapper;

import com.kz.internship_project.dto.attachment.AttachmentResponseDto;
import com.kz.internship_project.entity.Attachment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AttachmentMapper {

    @Mapping(target = "lessonId", source = "lesson.id")
    AttachmentResponseDto toDto(Attachment entity);
}
