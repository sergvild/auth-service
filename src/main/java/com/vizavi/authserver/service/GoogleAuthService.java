package com.vizavi.authserver.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.vizavi.authserver.config.AppProperties;
import com.vizavi.authserver.dto.response.AuthResponse;
import com.vizavi.authserver.entity.AuthProvider;
import com.vizavi.authserver.entity.ERole;
import com.vizavi.authserver.entity.RefreshToken;
import com.vizavi.authserver.entity.User;
import com.vizavi.authserver.exception.ResourceNotFoundException;
import com.vizavi.authserver.repository.RoleRepository;
import com.vizavi.authserver.repository.UserRepository;
import com.vizavi.authserver.security.JwtTokenProvider;
import com.vizavi.authserver.security.UserPrincipal;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service
public class GoogleAuthService {

    private final GoogleIdTokenVerifier verifier;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final AppProperties appProperties;

    public GoogleAuthService(AppProperties appProperties, UserRepository userRepository,
                             RoleRepository roleRepository, JwtTokenProvider jwtTokenProvider,
                             RefreshTokenService refreshTokenService) {
        this.appProperties = appProperties;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(appProperties.google().clientId()))
                .build();
    }

    @Transactional
    public AuthResponse authenticate(String idTokenString) {
        GoogleIdToken.Payload payload = verifyToken(idTokenString);
        String email = payload.getEmail();
        String providerId = payload.getSubject();

        User user = userRepository.findByEmail(email)
                .map(existing -> linkGoogleIfNeeded(existing, providerId))
                .orElseGet(() -> createUser(email, providerId));

        UserPrincipal principal = UserPrincipal.create(user);
        String accessToken = jwtTokenProvider.generateAccessToken(principal);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        return AuthResponse.of(accessToken, refreshToken.getToken(), appProperties.jwt().accessTokenExpirationMs());
    }

    private GoogleIdToken.Payload verifyToken(String idTokenString) {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new BadCredentialsException("Invalid Google ID token");
            }
            return idToken.getPayload();
        } catch (GeneralSecurityException | IOException e) {
            throw new BadCredentialsException("Google token verification failed");
        }
    }

    private User linkGoogleIfNeeded(User user, String providerId) {
        if (user.getProvider() == null) {
            user.setProvider(AuthProvider.GOOGLE);
            user.setProviderId(providerId);
            return userRepository.save(user);
        }
        return user;
    }

    private User createUser(String email, String providerId) {
        var role = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new ResourceNotFoundException("Default role not found"));
        return userRepository.save(User.builder()
                .email(email)
                .provider(AuthProvider.GOOGLE)
                .providerId(providerId)
                .roles(new HashSet<>(Set.of(role)))
                .build());
    }
}