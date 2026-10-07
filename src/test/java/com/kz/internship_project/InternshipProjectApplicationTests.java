package com.kz.internship_project;

import com.kz.internship_project.config.KeycloakConfig;
import com.kz.internship_project.config.KeycloakRoleConverter;
import com.kz.internship_project.service.KeycloakInitializer;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class InternshipProjectApplicationTests extends BaseIntegrationTest {

	@MockitoBean
	private Keycloak keycloak;

	@MockitoBean
	private KeycloakConfig keycloakConfig;

	@MockitoBean
	private KeycloakRoleConverter keycloakRoleConverter;

	@MockitoBean
	private KeycloakInitializer keycloakInitializer;

	@Test
	void contextLoads() {
	}

}
