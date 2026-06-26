package com.vizavi.authserver.controller;

import com.vizavi.authserver.dto.request.GoogleTokenRequest;
import com.vizavi.authserver.dto.request.LoginRequest;
import com.vizavi.authserver.dto.request.RefreshTokenRequest;
import com.vizavi.authserver.dto.request.RegisterRequest;
import com.vizavi.authserver.dto.response.AuthResponse;
import com.vizavi.authserver.dto.response.MessageResponse;
import com.vizavi.authserver.service.AuthService;
import com.vizavi.authserver.service.GoogleAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

    private final AuthService authService;
    private final GoogleAuthService googleAuthService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate and receive tokens")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotate refresh token and get a new access token")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refreshToken(request);
    }

    @PostMapping("/google")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Authenticate with Google ID token")
    public AuthResponse googleAuth(@Valid @RequestBody GoogleTokenRequest request) {
        return googleAuthService.authenticate(request.idToken());
    }

    @PostMapping("/logout")
    @Operation(summary = "Invalidate a refresh token")
    public ResponseEntity<MessageResponse> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.ok(new MessageResponse("Logged out successfully"));
    }
}
