package com.kz.internship_project.service.impl;

import com.kz.internship_project.dto.attachment.AttachmentDownloadDto;
import com.kz.internship_project.dto.attachment.AttachmentResponseDto;
import com.kz.internship_project.entity.Attachment;
import com.kz.internship_project.entity.Lesson;
import com.kz.internship_project.mapper.AttachmentMapper;
import com.kz.internship_project.repository.AttachmentRepository;
import com.kz.internship_project.repository.LessonRepository;
import com.kz.internship_project.service.AttachmentService;
import com.kz.internship_project.service.FileService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final LessonRepository lessonRepository;
    private final FileService fileService;
    private final AttachmentMapper attachmentMapper;

    @Transactional
    public AttachmentResponseDto uploadAttachment(MultipartFile file, Long lessonId){
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(()->new EntityNotFoundException("Урок не найден"));

        String fileUrl = fileService.uploadFile(file);

        Attachment attachment = new Attachment();
        attachment.setName(file.getOriginalFilename());
        attachment.setUrl(fileUrl);
        attachment.setLesson(lesson);

        Attachment saved = attachmentRepository.save(attachment);
        return attachmentMapper.toDto(saved);
    }

    @Transactional
    public AttachmentDownloadDto downloadAttachment(Long attachmentId){
    Attachment attachment = attachmentRepository.findById(attachmentId)
            .orElseThrow(()-> new EntityNotFoundException("Вложение не найдено"));

        InputStreamResource resource = fileService.downloadFile(attachment.getUrl());

        return new AttachmentDownloadDto(resource, attachment.getName());
    }

    @Override
    public void deleteAttachment(Long attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(()->new EntityNotFoundException("Вложение не найдено"));

        fileService.deleteFile(attachment.getUrl());
        attachmentRepository.delete(attachment);
        }
}
