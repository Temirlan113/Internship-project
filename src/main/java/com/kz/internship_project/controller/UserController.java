package com.kz.internship_project.controller;

import com.kz.internship_project.dto.user.ChangeUserRoleDto;
import com.kz.internship_project.dto.user.UserCreateDto;
import com.kz.internship_project.dto.user.UserUpdateDto;
import com.kz.internship_project.service.impl.KeyCloakServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final KeyCloakServiceImpl keyCloakService;

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserCreateDto userCreateDto) {
        UserRepresentation response = keyCloakService.createUser(userCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/update-profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updateMyProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UserUpdateDto updateDto
    ) {
        String currentUserId = jwt.getSubject();

        keyCloakService.updateUser(currentUserId, updateDto, false);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{userId}/role")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> changeUserRole(
            @PathVariable String userId,
            @Valid @RequestBody ChangeUserRoleDto dto
    ) {
        keyCloakService.changeUserRole(userId, dto.role());
        return ResponseEntity.noContent().build();
    }
}