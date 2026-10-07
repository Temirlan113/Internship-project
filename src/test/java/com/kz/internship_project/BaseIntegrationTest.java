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
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    // 1. PostgreSQL с явным указанием совместимости
    protected static final PostgreSQLContainer<?> postgres;

    // 2. GenericContainer вместо MinIOContainer (чтобы исключить проверки модуля Testcontainers)
    protected static final GenericContainer<?> minio;

    static {
        postgres = new PostgreSQLContainer<>(
                DockerImageName.parse("postgres:15-alpine").asCompatibleSubstituteFor("postgres")
        )
                .withDatabaseName("internship-project-db")
                .withUsername("postgres")
                .withPassword("satorugod1");

        minio = new GenericContainer<>(
                DockerImageName.parse("minio/minio:RELEASE.2024-01-16T16-07-38Z".toLowerCase())
        )
                .withEnv("MINIO_ROOT_USER", "admin")
                .withEnv("MINIO_ROOT_PASSWORD", "admin1234")
                .withCommand("server /data")
                .withExposedPorts(9000)
                .waitingFor(Wait.forHttp("/minio/health/ready").forPort(9000));

        // Стартуем контейнеры вручную в static-блоке (Singleton Pattern)
        // Это самый надежный способ для Spring Boot + Testcontainers
        postgres.start();
        minio.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Postgres
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // MinIO
        String minioUrl = "http://" + minio.getHost() + ":" + minio.getMappedPort(9000);
        registry.add("minio.url", () -> minioUrl);
        registry.add("minio.user", () -> "admin");
        registry.add("minio.password", () -> "admin1234");

        // OAuth2 Stub
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> "");
    }
}