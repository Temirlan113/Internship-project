package com.kz.internship_project.controller;

import com.kz.internship_project.dto.attachment.AttachmentDownloadDto;
import com.kz.internship_project.dto.attachment.AttachmentResponseDto;
import com.kz.internship_project.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentResponseDto> upload(@RequestParam("file") MultipartFile file, @RequestParam("lessonId") Long lessonId){
        AttachmentResponseDto response = attachmentService.uploadAttachment(file, lessonId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(value = "/download/{attachmentId}")
    public ResponseEntity<InputStreamResource> download(@PathVariable Long attachmentId) {
        AttachmentDownloadDto downloadDto = attachmentService.downloadAttachment(attachmentId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadDto.fileName() + "\"")
                .body(downloadDto.resource());
    }
    @DeleteMapping(value = "/{attachmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Void> delete(@PathVariable Long attachmentId){
        attachmentService.deleteAttachment(attachmentId);

        return ResponseEntity.noContent().build();
    }
}
