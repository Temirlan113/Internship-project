package com.kz.internship_project.service.impl;

import com.kz.internship_project.dto.attachment.*;
import com.kz.internship_project.entity.Attachment;
import com.kz.internship_project.entity.Lesson;
import com.kz.internship_project.mapper.AttachmentMapper;
import com.kz.internship_project.repository.AttachmentRepository;
import com.kz.internship_project.repository.LessonRepository;
import com.kz.internship_project.service.AttachmentService;
import com.kz.internship_project.service.FileService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
@Slf4j
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final LessonRepository lessonRepository;
    private final FileService fileService;
    private final AttachmentMapper attachmentMapper;


    @Transactional(readOnly = true)
    public AttachmentDownloadDto downloadAttachment(Long attachmentId) {
        Attachment attachment = getAttachmentOrThrow(attachmentId);

        InputStreamResource resource = fileService.downloadFile(attachment.getUrl());

        return new AttachmentDownloadDto(resource, attachment.getName());
    }

    @Override
    @Transactional
    public void deleteAttachment(Long attachmentId) {
        Attachment attachment = getAttachmentOrThrow(attachmentId);

        fileService.deleteFile(attachment.getUrl());
        attachmentRepository.delete(attachment);
    }

    public PresignedUrlResponseDto getPresignedUploadUrl(UploadRequestDto request) {

        String fileKey = UUID.randomUUID() + "_" + request.fileName();

        String uploadUrl = fileService.generatePresignedUploadUrl(fileKey);

        return new PresignedUrlResponseDto(uploadUrl, fileKey);

    }

    @Override
    @Transactional
    public AttachmentResponseDto confirmUpload(AttachmentConfirmDto dto) {
        Lesson lesson = lessonRepository.findById(dto.lessonId())
                .orElseThrow(() -> new EntityNotFoundException("Урок с ID " + dto.lessonId() + " не найден"));

        if (!fileService.exists(dto.fileKey())) {
            throw new IllegalArgumentException("Файл не найден в MinIO хранилище: " + dto.fileKey());
        }

        Attachment attachment = new Attachment();
        attachment.setName(dto.name());
        attachment.setUrl(dto.fileKey());
        attachment.setLesson(lesson);

        Attachment savedAttachment = attachmentRepository.save(attachment);

        return attachmentMapper.toDto(savedAttachment);
    }

    private Attachment getAttachmentOrThrow(Long attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new EntityNotFoundException("Вложение не найдено"));
    }


}
