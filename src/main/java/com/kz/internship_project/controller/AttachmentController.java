package com.kz.internship_project.controller;

import com.kz.internship_project.dto.attachment.*;
import com.kz.internship_project.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;


    @GetMapping(value = "/download/{attachmentId}")
    public ResponseEntity<InputStreamResource> download(@PathVariable Long attachmentId) {
        AttachmentDownloadDto downloadDto = attachmentService.downloadAttachment(attachmentId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .headers(headers -> headers.setContentDisposition(
                        ContentDisposition.attachment()
                                .filename(downloadDto.fileName(), StandardCharsets.UTF_8)
                                .build()))
                .body(downloadDto.resource());

    }

    @DeleteMapping(value = "/{attachmentId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TEACHER')")
    public ResponseEntity<Void> delete(@PathVariable Long attachmentId) {
        attachmentService.deleteAttachment(attachmentId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/presigned-upload-url")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TEACHER')")
    public ResponseEntity<PresignedUrlResponseDto> getUploadUrl(@Valid @RequestBody UploadRequestDto request) {
        PresignedUrlResponseDto response = attachmentService.getPresignedUploadUrl(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm")
    @Operation(summary = "Подтверждение загрузки файла и сохранение в БД")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TEACHER')")
    public ResponseEntity<AttachmentResponseDto> confirmUpload(@Valid @RequestBody AttachmentConfirmDto dto) {
        AttachmentResponseDto response = attachmentService.confirmUpload(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
