package com.vizavi.authserver.service;

import com.vizavi.authserver.config.AppProperties;
import com.vizavi.authserver.dto.request.LoginRequest;
import com.vizavi.authserver.dto.request.RefreshTokenRequest;
import com.vizavi.authserver.dto.request.RegisterRequest;
import com.vizavi.authserver.dto.response.AuthResponse;
import com.vizavi.authserver.dto.response.MessageResponse;
import com.vizavi.authserver.entity.*;
import com.vizavi.authserver.exception.ResourceNotFoundException;
import com.vizavi.authserver.exception.TokenRefreshException;
import com.vizavi.authserver.exception.UserAlreadyExistsException;
import com.vizavi.authserver.repository.RoleRepository;
import com.vizavi.authserver.repository.UserRepository;
import com.vizavi.authserver.repository.VerificationTokenRepository;
import com.vizavi.authserver.security.JwtTokenProvider;
import com.vizavi.authserver.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;
    private final AppProperties appProperties;

    @Transactional
    public MessageResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("Email already registered: " + request.email());
        }
        var userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new ResourceNotFoundException("Default role not found"));
        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .provider(AuthProvider.LOCAL)
                .enabled(false)
                .roles(new HashSet<>(Set.of(userRole)))
                .build();
        user = userRepository.save(user);

        String token = UUID.randomUUID().toString();
        verificationTokenRepository.save(VerificationToken.builder()
                .token(token)
                .user(user)
                .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .build());

        emailService.sendVerificationEmail(user, token);
        return new MessageResponse("Registration successful. Please check your email to verify your account.");
    }

    @Transactional
    public AuthResponse verifyEmail(String tokenValue) {
        log.debug("Looking up verification token");
        VerificationToken verificationToken = verificationTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> {
                    log.warn("Verification attempt with invalid token");
                    return new TokenRefreshException("Invalid verification token");
                });

        if (verificationToken.isExpired()) {
            log.warn("Expired verification token for user {}", verificationToken.getUser().getId());
            verificationTokenRepository.delete(verificationToken);
            throw new TokenRefreshException("Verification token has expired. Please register again.");
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        verificationTokenRepository.delete(verificationToken);
        log.info("Email verified successfully for user {}", user.getId());

        return buildAuthResponse(UserPrincipal.create(user), user);
    }

    public AuthResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        User user = userRepository.findByEmail(principal.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return buildAuthResponse(principal, user);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken newToken = refreshTokenService.verifyAndRotate(request.refreshToken());
        String accessToken = jwtTokenProvider.generateAccessToken(UserPrincipal.create(newToken.getUser()));
        return AuthResponse.of(accessToken, newToken.getToken(), appProperties.jwt().accessTokenExpirationMs());
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.deleteByToken(refreshToken);
    }

    private AuthResponse buildAuthResponse(UserPrincipal principal, User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(principal);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        return AuthResponse.of(accessToken, refreshToken.getToken(), appProperties.jwt().accessTokenExpirationMs());
    }
}