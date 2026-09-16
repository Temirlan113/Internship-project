package com.kz.internship_project.service;

import com.kz.internship_project.dto.attachment.AttachmentDownloadDto;
import com.kz.internship_project.dto.attachment.AttachmentResponseDto;
import org.springframework.web.multipart.MultipartFile;

public interface AttachmentService {
    AttachmentResponseDto uploadAttachment(MultipartFile file, Long lessonId);

    AttachmentDownloadDto downloadAttachment(Long attachmentId);

    void deleteAttachment(Long attachmentId);
}
