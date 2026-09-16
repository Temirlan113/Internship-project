package com.kz.internship_project.service;

import com.kz.internship_project.dto.auth.AuthResponseDto;
import com.kz.internship_project.dto.auth.JwtResponseDto;
import com.kz.internship_project.dto.auth.LoginCreateDto;
import com.kz.internship_project.dto.auth.RefreshTokenRequestDto;
import com.kz.internship_project.dto.user.UserCreateDto;
import com.kz.internship_project.dto.user.UserResponseDto;
import com.kz.internship_project.dto.user.UserUpdateDto;
import com.kz.internship_project.enums.RoleUser;



public interface KeycloakService {

    UserResponseDto createUser(UserCreateDto user);

    JwtResponseDto login(LoginCreateDto login);

    AuthResponseDto refreshToken(RefreshTokenRequestDto request);

    void updateUser(String userId, UserUpdateDto dto);

    void changeUserRole(String userId, RoleUser newRole);

}
