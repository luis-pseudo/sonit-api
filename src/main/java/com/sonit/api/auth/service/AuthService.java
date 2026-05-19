package com.sonit.api.auth.service;

import java.time.Instant;
import java.util.Random;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sonit.api.auth.dto.AuthResponse;
import com.sonit.api.auth.dto.LoginRequest;
import com.sonit.api.auth.dto.RefreshTokenRequest;
import com.sonit.api.auth.dto.RegisterRequest;
import com.sonit.api.common.exception.ConflictException;
import com.sonit.api.common.exception.ResourceNotFoundException;
import com.sonit.api.common.exception.UnauthorizedException;
import com.sonit.api.user.dto.UserSummaryDto;
import com.sonit.api.user.model.User;
import com.sonit.api.user.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final Random random = new Random();

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        // 1. Verify email does not exist
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already registered");
        }

        // 2. Generate unique username
        String username = generateUniqueUsername(request.getDisplayName());

        // 3. Hash password
        String passwordHash = passwordEncoder.encode(request.getPassword());

        // 4. Build and save User
        Instant now = Instant.now();
        User user = User.builder()
                .username(username)
                .displayName(request.getDisplayName())
                .email(request.getEmail())
                .passwordHash(passwordHash)
                .active(true)
                .visible(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        User savedUser = userRepository.save(user);

        return buildAuthResponse(savedUser);
        }

        public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("User not found for the provided email"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        Instant now = Instant.now();
        user.setLastSeenAt(now);
        user.setUpdatedAt(now);
        User savedUser = userRepository.save(user);

        return buildAuthResponse(savedUser);
        }

        public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtService.isTokenValid(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        String userId = jwtService.extractUserId(refreshToken);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return buildAuthResponse(user);
        }

        public void logout(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Instant now = Instant.now();
        user.setLastSeenAt(now);
        user.setUpdatedAt(now);
        userRepository.save(user);
        }

        private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        UserSummaryDto userSummary = UserSummaryDto.builder()
            .id(user.getId())
            .username(user.getUsername())
            .displayName(user.getDisplayName())
            .email(user.getEmail())
            .photoUrl(user.getPhotoUrl())
            .build();

        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .user(userSummary)
            .build();
    }

    private String generateUniqueUsername(String displayName) {
        String baseUsername = displayName.toLowerCase().replaceAll("[^a-z0-9_]", "");
        String username = baseUsername;

        int attempts = 0;
        while (userRepository.existsByUsername(username) && attempts < 10) {
            String randomSuffix = generateRandomString(5);
            username = baseUsername + "_" + randomSuffix;
            attempts++;
        }

        if (attempts == 10) {
            throw new RuntimeException("Failed to generate unique username");
        }

        return username;
    }

    private String generateRandomString(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
