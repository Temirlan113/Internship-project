package com.kz.internship_project.controller;

import com.kz.internship_project.dto.auth.AuthResponseDto;
import com.kz.internship_project.dto.auth.JwtResponseDto;
import com.kz.internship_project.dto.auth.LoginCreateDto;
import com.kz.internship_project.dto.auth.RefreshTokenRequestDto;
import com.kz.internship_project.service.impl.KeyCloakServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final KeyCloakServiceImpl keyCloakService;

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> login(@RequestBody LoginCreateDto login){
        return ResponseEntity.ok(keyCloakService.login(login));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponseDto> refreshToken(@Valid @RequestBody RefreshTokenRequestDto request){
        AuthResponseDto response = keyCloakService.refreshToken(request);
        return ResponseEntity.ok(response);
    }
}
