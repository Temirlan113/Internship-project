package com.kz.internship_project.utils;

import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import java.util.List;
import java.util.Map;

public class SecurityTestUtils {

    /**
     * Эмулирует JWT токен Keycloak с заданными ролями
     */
    public static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor keycloakJwt(String... roles) {
        return jwt().jwt(jwt -> jwt.claim("realm_access", Map.of("roles", List.of(roles))));
    }
}