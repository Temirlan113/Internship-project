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
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@ExtendWith(MockitoExtension.class)
class KeyCloakServiceImplTest {

    @Mock
    private AuthMapper authMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private Keycloak keycloakAdmin;

    @Mock
    private RealmResource realmResource;

    @Mock
    private UsersResource usersResource;

    @Mock
    private UserResource userResource;

    @Mock
    private RoleMappingResource roleMappingResource;

    @Mock
    private RoleScopeResource roleScopeResource;

    @Mock
    private RolesResource rolesResource;

    @Mock
    private RoleResource roleResource;

    private RestTemplate restTemplate;
    private MockRestServiceServer mockServer;

    private KeyCloakServiceImpl keycloakService;

    private final String realm = "test-realm";
    private final String serverUrl = "http://localhost:8080";
    private final String clientId = "test-client";
    private final String clientSecret = "test-secret";

    private UserCreateDto userCreateDto;
    private UserRepresentation fakeUserRepresentation;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);

        keycloakService = new KeyCloakServiceImpl(realm, authMapper, userMapper, keycloakAdmin, restTemplate);

        ReflectionTestUtils.setField(keycloakService, "serverUrl", serverUrl);
        ReflectionTestUtils.setField(keycloakService, "clientId", clientId);
        ReflectionTestUtils.setField(keycloakService, "clientSecret", clientSecret);

        userCreateDto = new UserCreateDto(
                "john_doe",
                "john@example.com",
                "password123",
                "John",
                "Doe",
                RoleUser.ROLE_USER
        );

        fakeUserRepresentation = new UserRepresentation();
        fakeUserRepresentation.setId("user-123");
        fakeUserRepresentation.setUsername("john_doe");
        fakeUserRepresentation.setEmail("john@example.com");
    }

    private void mockKeycloakFluentChain() {
        Mockito.when(keycloakAdmin.realm(realm)).thenReturn(realmResource);
        Mockito.when(realmResource.users()).thenReturn(usersResource);
        Mockito.when(usersResource.get(Mockito.anyString())).thenReturn(userResource);
        Mockito.when(realmResource.roles()).thenReturn(rolesResource);
        Mockito.when(rolesResource.get(Mockito.anyString())).thenReturn(roleResource);
        Mockito.when(userResource.roles()).thenReturn(roleMappingResource);
        Mockito.when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);
    }

    //----------------------------------
    //Позитивные
    //----------------------------------

    @Test
    void createUser_Success() {
        // Arrange
        mockKeycloakFluentChain();

        Response responseMock = Mockito.mock(Response.class);
        Mockito.when(responseMock.getStatusInfo()).thenReturn(Response.Status.CREATED);
        Mockito.when(responseMock.getStatus()).thenReturn(201);
        Mockito.when(responseMock.getLocation()).thenReturn(URI.create("http://localhost:8080/users/user-123"));

        Mockito.when(usersResource.create(Mockito.any(UserRepresentation.class))).thenReturn(responseMock);
        Mockito.when(roleResource.toRepresentation()).thenReturn(new RoleRepresentation("ROLE_USER", null, false));
        Mockito.when(userResource.toRepresentation()).thenReturn(fakeUserRepresentation);
        Mockito.when(userMapper.toDto(Mockito.any(), Mockito.any())).thenReturn(fakeUserRepresentation);

        // Act
        UserRepresentation result = keycloakService.createUser(userCreateDto);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals("user-123", result.getId());

        Mockito.verify(usersResource, Mockito.times(1)).create(Mockito.any(UserRepresentation.class));
        Mockito.verify(userResource, Mockito.times(1)).resetPassword(Mockito.any());
        Mockito.verify(roleScopeResource, Mockito.times(1)).add(Mockito.anyList());
    }

    @Test
    void login_Success() {
        // Arrange
        LoginCreateDto loginDto = new LoginCreateDto("john_doe", "password123");
        String expectedUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        String jsonResponse = """
                {
                    "access_token": "access-token-123",
                    "refresh_token": "refresh-token-123",
                    "expires_in": 300,
                    "token_type": "Bearer"
                }
                """;

        mockServer.expect(requestTo(expectedUrl))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        // Act
        JwtResponseDto response = keycloakService.login(loginDto);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals("access-token-123", response.accessToken());
        Assertions.assertEquals("refresh-token-123", response.refreshToken());
        Assertions.assertEquals(300L, response.expiresIn());
        mockServer.verify();
    }

    @Test
    void refreshToken_Success() {
        // Arrange
        RefreshTokenRequestDto requestDto = new RefreshTokenRequestDto("valid-refresh-token");
        String expectedUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        String jsonResponse = """
                {
                    "access_token": "new-access-token",
                    "refresh_token": "new-refresh-token",
                    "expires_in": 300
                }
                """;

        AuthResponseDto expectedAuthResponse = new AuthResponseDto("new-access-token", "new-refresh-token", 300L);

        mockServer.expect(requestTo(expectedUrl))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        Mockito.when(authMapper.toDto(Mockito.any(AccessTokenResponse.class))).thenReturn(expectedAuthResponse);

        // Act
        AuthResponseDto result = keycloakService.refreshToken(requestDto);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals("new-access-token", result.accessToken());
        mockServer.verify();
    }

    @Test
    void updateUser_Success() {
        // Arrange
        String userId = "user-123";
        UserUpdateDto updateDto = new UserUpdateDto(
                "new_email@example.com",
                "oldPass",
                "newPass123",
                "NewName",
                "NewLastName"
        );

        Mockito.when(keycloakAdmin.realm(realm)).thenReturn(realmResource);
        Mockito.when(realmResource.users()).thenReturn(usersResource);
        Mockito.when(usersResource.get(userId)).thenReturn(userResource);
        Mockito.when(userResource.toRepresentation()).thenReturn(fakeUserRepresentation);

        // Act
        keycloakService.updateUser(userId, updateDto, true);

        // Assert
        ArgumentCaptor<UserRepresentation> userCaptor = ArgumentCaptor.forClass(UserRepresentation.class);
        Mockito.verify(userResource, Mockito.times(1)).update(userCaptor.capture());

        UserRepresentation updatedUser = userCaptor.getValue();
        Assertions.assertEquals("NewName", updatedUser.getFirstName());
        Assertions.assertEquals("NewLastName", updatedUser.getLastName());
        Assertions.assertEquals("new_email@example.com", updatedUser.getEmail());

        Mockito.verify(userResource, Mockito.times(1)).resetPassword(Mockito.any());
    }

    @Test
    void changeUserRole_Success() {
        // Arrange
        String userId = "user-123";
        RoleUser newRole = RoleUser.ROLE_ADMIN;

        Mockito.when(keycloakAdmin.realm(realm)).thenReturn(realmResource);
        Mockito.when(realmResource.users()).thenReturn(usersResource);
        Mockito.when(usersResource.get(userId)).thenReturn(userResource);
        Mockito.when(realmResource.roles()).thenReturn(rolesResource);
        Mockito.when(rolesResource.get("ROLE_ADMIN")).thenReturn(roleResource);

        RoleRepresentation adminRoleRep = new RoleRepresentation("ROLE_ADMIN", null, false);
        RoleRepresentation oldUserRoleRep = new RoleRepresentation("ROLE_USER", null, false);

        Mockito.when(roleResource.toRepresentation()).thenReturn(adminRoleRep);
        Mockito.when(userResource.roles()).thenReturn(roleMappingResource);
        Mockito.when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);
        Mockito.when(roleScopeResource.listAll()).thenReturn(List.of(oldUserRoleRep));

        // Act
        keycloakService.changeUserRole(userId, newRole);

        // Assert
        Mockito.verify(roleScopeResource, Mockito.times(1)).remove(List.of(oldUserRoleRep));
        Mockito.verify(roleScopeResource, Mockito.times(1)).add(Collections.singletonList(adminRoleRep));
    }

    //----------------------------------
    //Негативные сценарии
    //----------------------------------

    @Test
    void createUser_KeycloakReturnsError_ThrowsRuntimeException() {
        // Arrange
        Mockito.when(keycloakAdmin.realm(realm)).thenReturn(realmResource);
        Mockito.when(realmResource.users()).thenReturn(usersResource);

        Response responseMock = Mockito.mock(Response.class);
        Mockito.when(responseMock.getStatus()).thenReturn(400); // 400 Bad Request
        Mockito.when(usersResource.create(Mockito.any(UserRepresentation.class))).thenReturn(responseMock);

        // Act & Assert
        RuntimeException exception = Assertions.assertThrows(
                RuntimeException.class,
                () -> keycloakService.createUser(userCreateDto)
        );

        Assertions.assertEquals("Не удалось создать пользователя", exception.getMessage());
        Mockito.verify(usersResource, Mockito.times(1)).create(Mockito.any(UserRepresentation.class));
    }

    @Test
    void createUser_RoleNotFound_ThrowsIllegalArgumentException() {
        // Arrange
        Mockito.when(keycloakAdmin.realm(realm)).thenReturn(realmResource);
        Mockito.when(realmResource.users()).thenReturn(usersResource);
        Mockito.when(usersResource.get(Mockito.anyString())).thenReturn(userResource);
        Mockito.when(realmResource.roles()).thenReturn(rolesResource);
        Mockito.when(rolesResource.get(Mockito.anyString())).thenReturn(roleResource);

        Response responseMock = Mockito.mock(Response.class);
        Mockito.when(responseMock.getStatusInfo()).thenReturn(Response.Status.CREATED);
        Mockito.when(responseMock.getStatus()).thenReturn(201);
        Mockito.when(responseMock.getLocation()).thenReturn(URI.create("http://localhost:8080/users/user-123"));
        Mockito.when(usersResource.create(Mockito.any(UserRepresentation.class))).thenReturn(responseMock);

        Mockito.when(roleResource.toRepresentation()).thenThrow(new RuntimeException("Role not found"));

        // Act & Assert
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> keycloakService.createUser(userCreateDto)
        );

        Assertions.assertEquals("Указанная роль не существует в системе: ROLE_USER", exception.getMessage());
    }

    @Test
    void refreshToken_InvalidToken_ThrowsBadCredentialsException() {
        // Arrange
        RefreshTokenRequestDto requestDto = new RefreshTokenRequestDto("invalid-token");
        String expectedUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        mockServer.expect(requestTo(expectedUrl))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withBadRequest().body("{\"error\":\"invalid_grant\"}"));

        // Act & Assert
        BadCredentialsException exception = Assertions.assertThrows(
                BadCredentialsException.class,
                () -> keycloakService.refreshToken(requestDto)
        );

        Assertions.assertEquals("Невалидный или просроченный Refresh Token", exception.getMessage());
        mockServer.verify();
    }

    @Test
    void updateUser_CallerNotAdminModifyingRoles_ThrowsSecurityException() {
        // Arrange
        String userId = "user-123";
        UserUpdateDto updateDto = new UserUpdateDto(
                "email@example.com",
                "oldPass",
                "newPass123",
                "John",
                "Doe"
        );

        Mockito.when(keycloakAdmin.realm(realm)).thenReturn(realmResource);
        Mockito.when(realmResource.users()).thenReturn(usersResource);
        Mockito.when(usersResource.get(userId)).thenReturn(userResource);
        Mockito.when(userResource.toRepresentation()).thenReturn(fakeUserRepresentation);

        SecurityException exception = Assertions.assertThrows(
                SecurityException.class,
                () -> keycloakService.updateUser(userId, updateDto, false)
        );

        Assertions.assertEquals("У вас нет прав на изменение ролей", exception.getMessage());
    }
}