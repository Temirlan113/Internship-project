package com.kz.internship_project.mapper;

import com.kz.internship_project.dto.auth.AuthResponseDto;
import org.keycloak.representations.AccessTokenResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(source = "token", target = "accessToken")
AuthResponseDto toDto(AccessTokenResponse keycloakResponse);
}
