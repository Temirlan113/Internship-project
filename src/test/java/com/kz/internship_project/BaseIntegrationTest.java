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

    // 1. Указываем явный домен docker.io, чтобы избежать путаницы с реестрами
    protected static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("docker.io/library/postgres:15-alpine")
                    .asCompatibleSubstituteFor("postgres")
    )
            .withDatabaseName("internship-project-db")
            .withUsername("postgres")
            .withPassword("satorugod1");

    // 2. Используем стандартный легкий образ MinIO и GenericContainer
    protected static final GenericContainer<?> minio = new GenericContainer<>(
            DockerImageName.parse("docker.io/minio/minio:RELEASE.2024-01-16T16-07-38Z".toLowerCase())
    )
            .withEnv("MINIO_ROOT_USER", "admin")
            .withEnv("MINIO_ROOT_PASSWORD", "admin1234")
            .withCommand("server /data")
            .withExposedPorts(9000)
            .waitingFor(Wait.forHttp("/minio/health/ready").forPort(9000));

    static {
        // Стартуем контейнеры строго до запуска Spring
        postgres.start();
        minio.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Postgres properties
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // MinIO properties
        String minioUrl = "http://" + minio.getHost() + ":" + minio.getMappedPort(9000);
        registry.add("minio.url", () -> minioUrl);
        registry.add("minio.user", () -> "admin");
        registry.add("minio.password", () -> "admin1234");

        // OAuth2 Stub
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> "");
    }
}