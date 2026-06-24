package com.vizavi.authserver.service;

import com.vizavi.authserver.config.AppProperties;
import com.vizavi.authserver.entity.RefreshToken;
import com.vizavi.authserver.entity.User;
import com.vizavi.authserver.exception.TokenRefreshException;
import com.vizavi.authserver.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AppProperties appProperties;

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(appProperties.jwt().refreshTokenExpirationMs()))
                .build();
        return refreshTokenRepository.save(token);
    }

    @Transactional
    public RefreshToken verifyAndRotate(String rawToken) {
        RefreshToken token = refreshTokenRepository.findByToken(rawToken)
                .orElseThrow(() -> new TokenRefreshException("Refresh token not found"));
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException("Refresh token has expired. Please log in again.");
        }
        return createRefreshToken(token.getUser());
    }

    @Transactional
    public void deleteByToken(String rawToken) {
        refreshTokenRepository.findByToken(rawToken).ifPresent(refreshTokenRepository::delete);
    }
}
