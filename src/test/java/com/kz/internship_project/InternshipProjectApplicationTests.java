package com.kz.internship_project;

import com.kz.internship_project.service.KeycloakInitializer;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class InternshipProjectApplicationTests {

	@MockitoBean
	private Keycloak keycloak;

	@MockitoBean
	private KeycloakInitializer keycloakInitializer;

	@Test
	void contextLoads() {
	}

}
