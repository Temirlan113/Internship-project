package com.kz.internship_project.config;

import com.kz.internship_project.enums.RoleUser;
import com.kz.internship_project.exception.CustomAccessDeniedHandler;
import com.kz.internship_project.exception.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
        return converter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, JwtAuthenticationConverter jwtAuthenticationConverter) {

        String admin = RoleUser.ROLE_ADMIN.name();
        String teacher = RoleUser.ROLE_TEACHER.name();
        String user = RoleUser.ROLE_USER.name();
        String student = RoleUser.ROLE_STUDENT.name();

        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception->exception.authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/api/v1/auth/**"
                        ).permitAll()

                        // 2. Управление пользователями (User Controller)
                        // Административные действия: создание пользователей и смена роли
                        .requestMatchers(HttpMethod.POST, "/api/v1/users/create").hasAuthority(admin)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/users/*/role").hasAuthority(admin)
                        .requestMatchers(HttpMethod.POST, "/api/v1/files/*").hasAuthority(admin)

                        // Свой профиль могут обновлять ВСЕ аутентифицированные пользователи
                        .requestMatchers(HttpMethod.PUT, "/api/v1/users/update-profile").authenticated()

                        // 3. Уроки (Lesson Controller)
                        //  Только Админ может создавать и обновлять уроки
                        .requestMatchers(HttpMethod.POST, "/api/v1/lessons/**").hasAnyAuthority(admin)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/lessons/**").hasAnyAuthority(admin)
                        // Удалять уроки может ТОЛЬКО Админ
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/lessons/**").hasAuthority(admin)

                        // 4. Главы и Курсы (Chapter & Course Controllers)
                        // Создавать, изменять и удалять главы/курсы может ТОЛЬКО Админ
                        .requestMatchers(HttpMethod.POST, "/api/v1/chapters/**", "/api/v1/courses/**").hasAuthority(admin)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/chapters/**", "/api/v1/courses/**").hasAuthority(admin)
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/chapters/**", "/api/v1/courses/**").hasAuthority(admin)

                        // 5. Просмотр материалов (GET-запросы)
                        // Просматривать курсы, главы и уроки могут Teacher, Student, Admin
                        .requestMatchers(HttpMethod.GET, "/api/v1/courses/**", "/api/v1/chapters/**", "/api/v1/lessons/**")
                        .hasAnyAuthority(admin, teacher, student)

                        //5.1. Просмотр курсов(GET-запросы)
                        //User может просматривать только курсы. Он не имеет доступа к главам курсов и урокам
                        .requestMatchers(HttpMethod.GET, "/api/v1/courses/**")
                        .hasAnyAuthority(admin, teacher, student, user)

                        // 6. Скачивание вложений
                        // Добавлять вложения может только админ и учитель(admin, teacher)
                        // Скачивать могут все аутентифицированные пользователи(кроме user, он не купил курс)
                        .requestMatchers(HttpMethod.POST, "/api/v1/attachments/upload").hasAnyAuthority(admin,teacher)
                        .requestMatchers(HttpMethod.GET, "/api/v1/attachments/download/**").hasAnyAuthority(admin,teacher,student)

                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                );

        return httpSecurity.build();
    }
}