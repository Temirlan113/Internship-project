package com.kz.internship_project.dto.attachment;

import jakarta.validation.constraints.NotBlank;

public record UploadRequestDto(
        @NotBlank(message = "Имя файла не может быть пустым")
        String fileName
) {}