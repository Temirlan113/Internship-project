package com.kz.internship_project.service;

import com.kz.internship_project.dto.auth.AuthResponseDto;
import com.kz.internship_project.dto.auth.JwtResponseDto;
import com.kz.internship_project.dto.auth.LoginCreateDto;
import com.kz.internship_project.dto.auth.RefreshTokenRequestDto;
import com.kz.internship_project.dto.user.UserCreateDto;
import com.kz.internship_project.dto.user.UserUpdateDto;
import com.kz.internship_project.enums.RoleUser;
import org.keycloak.representations.idm.UserRepresentation;


public interface KeycloakService {

    UserRepresentation createUser(UserCreateDto user);

    JwtResponseDto login(LoginCreateDto login);

    AuthResponseDto refreshToken(RefreshTokenRequestDto request);

    void updateUser(String userId, UserUpdateDto dto, boolean isCallerAdmin);

    void changeUserRole(String userId, RoleUser newRole);

}
