package com.alexivashchenko.auth.service.controller;

import com.alexivashchenko.auth.service.dto.LoginRequest;
import com.alexivashchenko.auth.service.dto.RegisterRequest;
import com.alexivashchenko.auth.service.dto.TokenResponse;
import com.alexivashchenko.auth.service.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletResponse response) {
        TokenResponse tokens = authService.login(request);

        ResponseCookie refreshCookie = buildRefreshCookie(tokens.refreshToken());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity.ok(
                new TokenResponse(tokens.accessToken(), null)
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        TokenResponse tokens = authService.refresh(refreshToken);

        ResponseCookie refreshCookie = buildRefreshCookie(tokens.refreshToken());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity.ok(
                new TokenResponse(tokens.accessToken(), null)
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {

        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false) // true in prod
                .sameSite("Strict") // None in prod
                .path("/api/v1/auth/refresh")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());

        return ResponseEntity.noContent().build();
    }

    private ResponseCookie buildRefreshCookie(String refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false) // true in prod (HTTPS)
                .sameSite("Strict") // None in prod
                .path("/api/v1/auth/refresh")
                .maxAge(Duration.ofDays(30))
                .build();
    }

}

