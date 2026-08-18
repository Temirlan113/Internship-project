package com.kz.internship_project.config.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "LMS Management API",
                version = "1.0",
                description = "REST API для управления главами, курсами, уроками"
        )
)
public class OpenApiConfig {
}
