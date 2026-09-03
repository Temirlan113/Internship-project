package com.kz.internship_project.dto.user;

import com.kz.internship_project.enums.RoleUser;
import jakarta.validation.constraints.NotNull;

public record ChangeUserRoleDto(@NotNull(message = "Роль не может быть пустой")
                                RoleUser role) {
}
