package com.kz.internship_project.dto.user;

import com.kz.internship_project.enums.RoleUser;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UserUpdateDto(

        @NotBlank(message = "Email не может быть пустым")
        @Email(message = "Некорректный формат Email")
        String email,

        @NotBlank(message = "Пароль не может быть пустым")
        @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
        String currentPassword,

        @NotBlank(message = "Пароль не может быть пустым")
        @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
        String newPassword,

        @NotBlank(message = "Имя не может быть пустым")
        String firstName,

        @NotBlank(message = "Фамилия не может быть пустой")
        String lastName

) {
}
