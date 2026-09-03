package com.kz.internship_project.service.impl;

import com.kz.internship_project.dto.auth.AuthResponseDto;
import com.kz.internship_project.dto.auth.JwtResponseDto;
import com.kz.internship_project.dto.auth.LoginCreateDto;
import com.kz.internship_project.dto.auth.RefreshTokenRequestDto;
import com.kz.internship_project.dto.user.UserCreateDto;
import com.kz.internship_project.dto.user.UserUpdateDto;
import com.kz.internship_project.enums.RoleUser;
import com.kz.internship_project.mapper.AuthMapper;
import com.kz.internship_project.mapper.UserMapper;
import com.kz.internship_project.service.KeycloakService;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
public class KeyCloakServiceImpl implements KeycloakService {

    @Value("${keycloak.url}")
    private String serverUrl;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;


    private final String realm;
    private final AuthMapper authMapper;
    private final UserMapper userMapper;
    private final Keycloak keycloakAdmin;
    private final RestTemplate restTemplate;


    public KeyCloakServiceImpl(@Value("${keycloak.realm}") String realm, AuthMapper authMapper, UserMapper userMapper, Keycloak keycloakAdmin, RestTemplate restTemplate) {
        this.realm = realm;
        this.authMapper = authMapper;
        this.userMapper = userMapper;
        this.keycloakAdmin = keycloakAdmin;
        this.restTemplate = restTemplate;
    }


    @Override
    public UserRepresentation createUser(UserCreateDto user) {

        RealmResource realmResource = keycloakAdmin.realm(realm);
        UsersResource usersResource = realmResource.users();


        UserRepresentation newUser = new UserRepresentation();
        newUser.setUsername(user.username());
        newUser.setEmail(user.email());
        newUser.setFirstName(user.firstName());
        newUser.setLastName(user.lastName());
        newUser.setEmailVerified(true);
        newUser.setEnabled(true);

        try (Response response = usersResource.create(newUser)) {

            if (response.getStatus() != 201) {
                log.error("Ошибка при создании пользователя в KeyCloak. Статус: {}", response.getStatus());
                throw new RuntimeException("Не удалось создать пользователя");
            }

            String userId = CreatedResponseUtil.getCreatedId(response);

            CredentialRepresentation passwordCred = new CredentialRepresentation();

            passwordCred.setTemporary(false);
            passwordCred.setType(CredentialRepresentation.PASSWORD);
            passwordCred.setValue(user.password());
            usersResource.get(userId).resetPassword(passwordCred);

            assignRoleToUser(realmResource, userId, user.role());


            UserRepresentation createdUserRepresentation = usersResource.get(userId).toRepresentation();
            return userMapper.toDto(createdUserRepresentation, user.role());
        }
    }

    private void assignRoleToUser(RealmResource realmResource, String userId, RoleUser roleName) {

        String roleNameStr = roleName.name();
        try {
            RoleRepresentation role = realmResource.roles().get(roleNameStr).toRepresentation();


            realmResource.users().get(userId).roles().realmLevel().add(Collections.singletonList(role));
            log.info("Роль {} успешно назначена пользователю с ID: {}", roleNameStr, userId);
        } catch (Exception e) {
            log.error("Не удалось найти или назначить роль {}: {}", roleNameStr, e.getMessage());
            throw new IllegalArgumentException("Указанная роль не существует в системе: " + roleNameStr);
        }
    }

    @Override
    public JwtResponseDto login(LoginCreateDto login) {

        String tokenUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("username", login.username());
        map.add("password", login.password());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<JwtResponseDto> response = restTemplate.postForEntity(tokenUrl, request, JwtResponseDto.class);

        return response.getBody();
    }

    @Override
    public AuthResponseDto refreshToken(RefreshTokenRequestDto request) {

        String tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token", serverUrl, realm);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "refresh_token");
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("refresh_token", request.refreshToken());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

        try {
            ResponseEntity<AccessTokenResponse> response = restTemplate.postForEntity(tokenUrl, entity, AccessTokenResponse.class);

            return authMapper.toDto(response.getBody());
        } catch (HttpClientErrorException e) {
            log.error("Ошибка обновления токена в Keycloak: {}", e.getResponseBodyAsString());
            throw new BadCredentialsException("Невалидный или просроченный Refresh Token");
        }
    }

    @Override
    public void updateUser(String userId, UserUpdateDto dto, boolean isCallerAdmin) {

        RealmResource realmResource = keycloakAdmin.realm(realm);
        UserResource userResource = realmResource.users().get(userId);

        UserRepresentation user = userResource.toRepresentation();

        if (dto.firstName() != null) user.setFirstName(dto.firstName());
        if (dto.lastName() != null) user.setLastName(dto.lastName());
        if (dto.email() != null) user.setEmail(dto.email());

        userResource.update(user);
        log.info("Базовые данные пользователя {} обновлены", userId);

        if (dto.newPassword() != null && !dto.newPassword().isBlank()) {
            changePassword(userResource, dto.newPassword());
        }
    }

    private void changePassword(UserResource userResource, String newPassword) {
        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(newPassword);

        userResource.resetPassword(passwordCred);
        log.info("Пароль успешно изменен");
    }

    public void changeUserRole(String userId, RoleUser newRole){
        RealmResource realmResource = keycloakAdmin.realm(realm);
        UserResource userResource = realmResource.users().get(userId);

        String newRoleName = newRole.name();

        RoleRepresentation targetRole = realmResource.roles().get(newRoleName).toRepresentation();

        List<RoleRepresentation> userCurrentRoles = userResource.roles().realmLevel().listAll();

        Set<String> allAppRoles = Arrays.stream(RoleUser.values())
                .map(Enum::name)
                .collect(Collectors.toSet());

        List<RoleRepresentation> rolesToRemove = userCurrentRoles.stream()
                .filter(role ->allAppRoles.contains(role.getName()))
                .collect(Collectors.toList());

        if (!rolesToRemove.isEmpty()) {
            userResource.roles().realmLevel().remove(rolesToRemove);
        }

        userResource.roles().realmLevel().add(Collections.singletonList(targetRole));

        log.info("Роль пользователя с ID {} успешно изменена на {}", userId, newRoleName);
    }
}





