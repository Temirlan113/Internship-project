package com.kz.internship_project;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Подключаемся к сервисам, запущенным GitHub Actions
        registry.add("spring.datasource.url", () -> "jdbc:postgresql://localhost:5432/internship-project-db");
        registry.add("spring.datasource.username", () -> "postgres");
        registry.add("spring.datasource.password", () -> "satorugod1");

        registry.add("minio.url", () -> "http://localhost:9000");
        registry.add("minio.user", () -> "admin");
        registry.add("minio.password", () -> "admin1234");

        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> "");
    }
}