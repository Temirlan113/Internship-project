package com.kz.internship_project.service;

import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;


@Service
@Slf4j
public class KeycloakInitializer {

    @Value("${keycloak.realm}")
    private String realmName;

    private final Keycloak keycloakAdmin;

    public KeycloakInitializer(Keycloak keycloakAdmin) {
        this.keycloakAdmin = keycloakAdmin;
    }


    @PostConstruct
    public void initDefaultUsers() {

        RealmResource realmResource = keycloakAdmin.realm(realmName);
        UsersResource usersResource = realmResource.users();

        if (!usersResource.search("appAdmin").isEmpty()) {
            log.info("Пользователь 'appAdmin' уже существует в Keycloak. Пропуск.");
            return;
        }

        UserRepresentation newUser = new UserRepresentation();
        newUser.setUsername("appAdmin");
        newUser.setEmail("admin@example.com");
        newUser.setFirstName("Админ");
        newUser.setLastName("Админ");
        newUser.setEmailVerified(true);
        newUser.setEnabled(true);

        Response response = usersResource.create(newUser);
            if (response.getStatus() == 201) {
                String userId = CreatedResponseUtil.getCreatedId(response);
                UserResource userResource = usersResource.get(userId);

                CredentialRepresentation passwordCred = new CredentialRepresentation();
                passwordCred.setTemporary(false);
                passwordCred.setType(CredentialRepresentation.PASSWORD);
                passwordCred.setValue("password123");
                usersResource.get(userId).resetPassword(passwordCred);

                log.info("Пользователь 'appAdmin' успешно создан с паролем!");
                assignAdminRole(realmResource, userResource, userId);

            } else {
                log.error("Не удалось создать пользователя. Код ответа: {}", response.getStatus());
            }
        }

    private void assignAdminRole(RealmResource realmResource, UserResource userResource, String userId) {
        try {
            RoleRepresentation adminRole = realmResource.roles().get("ROLE_ADMIN").toRepresentation();

            userResource.roles().realmLevel().add(Collections.singletonList(adminRole));
            log.info("Роль '{}' успешно назначена пользователю 'appAdmin' (ID: {})", "ROLE_ADMIN", userId);

        } catch (Exception e) {
            log.error("Не удалось назначить роль '{}' пользователю: {}", "ROLE_ADMIN", e.getMessage());
        }
    }

    }
