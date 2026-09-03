package com.kz.internship_project.dto.user;

import com.kz.internship_project.enums.RoleUser;

public record UserResponseDto(
        String id,
        String username,
        String email,
        RoleUser role,
        Boolean enabled,
        Boolean emailVerified
) {}
