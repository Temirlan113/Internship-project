package com.kz.internship_project.dto.user;

import com.kz.internship_project.enums.RoleUser;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserCreateDto(

        @NotBlank(message = "Username не может быть пустым")
        String username,

        @NotBlank(message = "Email не может быть пустым")
        @Email(message = "Некорректный формат Email")
        String email,

        @NotBlank(message = "Пароль не может быть пустым")
        @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
        String password,

        String firstName,
        String lastName,

        @NotNull(message = "Роль обязательна к указанию")
        RoleUser role
) {
}
