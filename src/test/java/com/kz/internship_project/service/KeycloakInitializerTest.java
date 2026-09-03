package com.kz.internship_project.service;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URI;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class KeycloakInitializerTest {

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

    private KeycloakInitializer keycloakInitializer;
    private final String realmName = "test-realm";

    @BeforeEach
    void setUp() {
        keycloakInitializer = new KeycloakInitializer(keycloakAdmin);
        ReflectionTestUtils.setField(keycloakInitializer, "realmName", realmName);
    }

    private void mockKeycloakFluentChain() {
        Mockito.when(keycloakAdmin.realm(realmName)).thenReturn(realmResource);
        Mockito.when(realmResource.users()).thenReturn(usersResource);
    }

    //----------------------------------
    //Позитивные сценарии
    //----------------------------------
    @Test
    void initDefaultUsers_UserDoesNotExist_CreatesAdminSuccessfully() {
        // Arrange
        mockKeycloakFluentChain();

        Mockito.when(usersResource.search("appAdmin")).thenReturn(Collections.emptyList());

        Response responseMock = Mockito.mock(Response.class);
        Mockito.when(responseMock.getStatusInfo()).thenReturn(Response.Status.CREATED);
        Mockito.when(responseMock.getStatus()).thenReturn(201);
        Mockito.when(responseMock.getLocation()).thenReturn(URI.create("http://localhost:8080/users/admin-id-123"));

        Mockito.when(usersResource.create(Mockito.any(UserRepresentation.class))).thenReturn(responseMock);
        Mockito.when(usersResource.get("admin-id-123")).thenReturn(userResource);

        Mockito.when(realmResource.roles()).thenReturn(rolesResource);
        Mockito.when(rolesResource.get("ROLE_ADMIN")).thenReturn(roleResource);
        Mockito.when(roleResource.toRepresentation()).thenReturn(new RoleRepresentation("ROLE_ADMIN", null, false));
        Mockito.when(userResource.roles()).thenReturn(roleMappingResource);
        Mockito.when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);

        // Act
        keycloakInitializer.initDefaultUsers();

        // Assert
        Mockito.verify(usersResource, Mockito.times(1)).create(Mockito.any(UserRepresentation.class));
        Mockito.verify(userResource, Mockito.times(1)).resetPassword(Mockito.any());
        Mockito.verify(roleScopeResource, Mockito.times(1)).add(Mockito.anyList());
    }

    @Test
    void initDefaultUsers_UserAlreadyExists_SkipsCreation() {
        // Arrange
        mockKeycloakFluentChain();

        UserRepresentation existingUser = new UserRepresentation();
        existingUser.setUsername("appAdmin");

        Mockito.when(usersResource.search("appAdmin")).thenReturn(List.of(existingUser));

        // Act
        keycloakInitializer.initDefaultUsers();

        // Assert

        Mockito.verify(usersResource, Mockito.never()).create(Mockito.any());
    }

    //----------------------------------
    //Негативные сценарии
    //----------------------------------

    @Test
    void initDefaultUsers_KeycloakReturnsErrorStatus_LogsErrorAndDoesNotSetPassword() {
        // Arrange
        mockKeycloakFluentChain();
        Mockito.when(usersResource.search("appAdmin")).thenReturn(Collections.emptyList());

        Response responseMock = Mockito.mock(Response.class);
        Mockito.when(responseMock.getStatus()).thenReturn(400);

        Mockito.when(usersResource.create(Mockito.any(UserRepresentation.class))).thenReturn(responseMock);

        // Act
        keycloakInitializer.initDefaultUsers();

        // Assert
        Mockito.verify(usersResource, Mockito.times(1)).create(Mockito.any(UserRepresentation.class));
        Mockito.verify(usersResource, Mockito.never()).get(Mockito.anyString());
    }

    @Test
    void initDefaultUsers_AssignRoleFails_HandlesExceptionGracefully() {
        // Arrange
        mockKeycloakFluentChain();
        Mockito.when(usersResource.search("appAdmin")).thenReturn(Collections.emptyList());

        Response responseMock = Mockito.mock(Response.class);
        Mockito.when(responseMock.getStatusInfo()).thenReturn(Response.Status.CREATED);
        Mockito.when(responseMock.getStatus()).thenReturn(201);
        Mockito.when(responseMock.getLocation()).thenReturn(URI.create("http://localhost:8080/users/admin-id-123"));

        Mockito.when(usersResource.create(Mockito.any(UserRepresentation.class))).thenReturn(responseMock);
        Mockito.when(usersResource.get("admin-id-123")).thenReturn(userResource);

        Mockito.when(realmResource.roles()).thenReturn(rolesResource);
        Mockito.when(rolesResource.get("ROLE_ADMIN")).thenReturn(roleResource);

        Mockito.when(roleResource.toRepresentation()).thenThrow(new RuntimeException("Role ROLE_ADMIN not found"));

        // Act
        keycloakInitializer.initDefaultUsers();

        // Assert
        Mockito.verify(userResource, Mockito.times(1)).resetPassword(Mockito.any());
    }
}