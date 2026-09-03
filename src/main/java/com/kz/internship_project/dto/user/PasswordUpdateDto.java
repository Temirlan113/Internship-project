package com.kz.internship_project.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordUpdateDto(

        @NotBlank(message = "Пароль не может быть пустым")
        @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
        String oldPassword,

        @NotBlank(message = "Пароль не может быть пустым")
        @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
        String newPassword
) {
}
