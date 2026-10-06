package com.kz.internship_project;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    // 1. Поднимаем реальный PostgreSQL в контейнере
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("internship-project-db")
            .withUsername("postgres")
            .withPassword("satorugod1")
    .withStartupTimeout(Duration.ofMinutes(5))
            .withStartupAttempts(3)
            .waitingFor(Wait.forHttp("/minio/health/live").forStatusCode(200));

    // 2. Поднимаем MinIO в контейнере
    @Container
    static GenericContainer<?> minio = new GenericContainer<>("quay.io/minio/minio:RELEASE.2023-09-04T19-57-37Z")
            .withExposedPorts(9000)
            .withEnv("MINIO_ROOT_USER", "admin")
            .withEnv("MINIO_ROOT_PASSWORD", "admin1234")
            .withCommand("server /data")
            .withStartupTimeout(Duration.ofMinutes(5))
            .waitingFor(Wait.forListeningPort());

    // Заменяем свойства application.properties динамическими значениями из запущенных контейнеров
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Postgres
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // MinIO
        registry.add("minio.url", () -> "http://" + minio.getHost() + ":" + minio.getMappedPort(9000));
        registry.add("minio.user", () -> "admin");
        registry.add("minio.password", () -> "admin1234");

        // Отключаем обязательную валидацию issuer-uri Keycloak при старте контекста
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> "");
    }
}