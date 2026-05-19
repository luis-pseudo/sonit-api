package com.sonit.api.auth.service;

import com.sonit.api.config.AppProperties;
import com.sonit.api.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        AppProperties properties = new AppProperties();
        AppProperties.Jwt jwtProperties = new AppProperties.Jwt();
        jwtProperties.setSecret("test-secret");
        jwtProperties.setExpiration(3600000L);
        jwtProperties.setRefreshExpiration(7200000L);
        properties.setJwt(jwtProperties);

        jwtService = new JwtService(properties);
        user = User.builder()
                .id("user-123")
                .email("test@example.com")
                .username("tester")
                .build();
    }

    @Test
    void shouldGenerateValidAccessTokenWithExpectedClaims() {
        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractUserId(token)).isEqualTo("user-123");
        assertThat(jwtService.isRefreshToken(token)).isFalse();
    }

    @Test
    void shouldGenerateValidRefreshToken() {
        String token = jwtService.generateRefreshToken(user);

        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractUserId(token)).isEqualTo("user-123");
        assertThat(jwtService.isRefreshToken(token)).isTrue();
    }

    @Test
    void shouldRejectTamperedToken() {
        String token = jwtService.generateAccessToken(user);
        String tamperedToken = token + "tampered";

        assertThat(jwtService.isTokenValid(tamperedToken)).isFalse();
        assertThat(jwtService.isRefreshToken(tamperedToken)).isFalse();
    }

    @Test
    void shouldRejectExpiredToken() {
        AppProperties properties = new AppProperties();
        AppProperties.Jwt jwtProperties = new AppProperties.Jwt();
        jwtProperties.setSecret("test-secret");
        jwtProperties.setExpiration(-1000L);
        jwtProperties.setRefreshExpiration(7200000L);
        properties.setJwt(jwtProperties);
        JwtService expiredJwtService = new JwtService(properties);

        String expiredToken = expiredJwtService.generateAccessToken(user);

        assertThat(expiredJwtService.isTokenValid(expiredToken)).isFalse();
    }
}
