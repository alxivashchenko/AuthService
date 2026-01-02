package com.alexivashchenko.auth.service.service;

import com.alexivashchenko.auth.service.entity.RefreshToken;
import com.alexivashchenko.auth.service.entity.User;
import com.alexivashchenko.auth.service.exception.InvalidRefreshTokenException;
import com.alexivashchenko.auth.service.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private static final long REFRESH_TOKEN_DAYS = 30;

    @Transactional
    public String create(User user) {
        repository.deleteByUser(user); // rotation

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiresAt(Instant.now().plus(REFRESH_TOKEN_DAYS, ChronoUnit.DAYS));

        repository.save(token);
        return token.getToken();
    }

    @Transactional
    public User getUserByValidRefreshToken(String tokenValue) {
        RefreshToken token = repository.findByToken(tokenValue)
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));

        if (token.getExpiresAt().isBefore(Instant.now())) {
            repository.delete(token);
            throw new InvalidRefreshTokenException("Refresh token expired");
        }

        return token.getUser();
    }
}
