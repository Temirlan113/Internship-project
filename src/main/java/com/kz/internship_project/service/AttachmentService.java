package com.kz.internship_project.service;

import com.kz.internship_project.dto.attachment.*;
import org.springframework.web.multipart.MultipartFile;

public interface AttachmentService {

    AttachmentDownloadDto downloadAttachment(Long attachmentId);

    void deleteAttachment(Long attachmentId);

    PresignedUrlResponseDto getPresignedUploadUrl(UploadRequestDto request);

    AttachmentResponseDto confirmUpload(AttachmentConfirmDto dto);
}
