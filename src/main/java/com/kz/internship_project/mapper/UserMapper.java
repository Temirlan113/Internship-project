package com.kz.internship_project.mapper;

import com.kz.internship_project.dto.user.UserResponseDto;
import com.kz.internship_project.enums.RoleUser;
import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

     UserResponseDto toDto(UserRepresentation keycloakUser, RoleUser role);
}
