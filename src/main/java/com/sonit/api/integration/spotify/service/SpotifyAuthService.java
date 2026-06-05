package com.sonit.api.integration.spotify.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.sonit.api.common.exception.ResourceNotFoundException;
import com.sonit.api.common.exception.UnauthorizedException;
import com.sonit.api.config.AppProperties;
import com.sonit.api.integration.spotify.dto.SpotifyTokenResponse;
import com.sonit.api.integration.spotify.dto.SpotifyUserProfileResponse;
import com.sonit.api.user.model.User;
import com.sonit.api.user.model.UserTokens;
import com.sonit.api.user.repository.UserRepository;
import com.sonit.api.user.repository.UserTokensRepository;

@Service
public class SpotifyAuthService {

    private static final String SPOTIFY_SERVICE = "spotify";
    private static final String SPOTIFY_ME_URL = "https://api.spotify.com/v1/me";

    private final AppProperties appProperties;
    private final UserRepository userRepository;
    private final UserTokensRepository userTokensRepository;
    private final RestClient restClient;

    private final Map<String, PkceSession> pkceSessionsByState = new ConcurrentHashMap<>();

    public SpotifyAuthService(
            AppProperties appProperties,
            UserRepository userRepository,
            UserTokensRepository userTokensRepository) {
        this.appProperties = appProperties;
        this.userRepository = userRepository;
        this.userTokensRepository = userTokensRepository;
        this.restClient = RestClient.builder().build();
    }

    public String generateAuthUrl(String userId) {
        cleanupExpiredPkceSessions();

        String codeVerifier = generateCodeVerifier();
        String codeChallenge = generateCodeChallenge(codeVerifier);
        String state = generateState();

        pkceSessionsByState.put(state, new PkceSession(userId, codeVerifier, Instant.now()));

        return buildSpotifyAuthUrl(codeChallenge, state);
    }

    public void handleCallback(String code, String state) {
        PkceSession session = pkceSessionsByState.remove(state);
        if (session == null) {
            throw new UnauthorizedException("Invalid or expired OAuth state");
        }

        SpotifyTokenResponse tokenResponse = exchangeCodeForTokens(code, session.codeVerifier());

        User user = userRepository.findById(session.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Instant now = Instant.now();
        upsertUserTokens(user.getId(), tokenResponse, now);
        enrichAndUpdateSpotifyConnection(user, tokenResponse.getAccessToken(), now);
    }

    private void upsertUserTokens(String userId, SpotifyTokenResponse tokenResponse, Instant now) {
        UserTokens userTokens = userTokensRepository.findByUserIdAndService(userId, SPOTIFY_SERVICE)
                .orElse(UserTokens.builder()
                        .userId(userId)
                        .service(SPOTIFY_SERVICE)
                        .build());

        userTokens.setAccessToken(tokenResponse.getAccessToken());
        userTokens.setRefreshToken(tokenResponse.getRefreshToken());
        userTokens.setExpiresAt(now.plusSeconds(tokenResponse.getExpiresIn()));

        userTokensRepository.save(userTokens);
    }

    private void enrichAndUpdateSpotifyConnection(User user, String accessToken, Instant now) {
        SpotifyUserProfileResponse spotifyProfile = fetchSpotifyProfile(accessToken);

        Map<String, User.StreamingService> connectedServices = user.getConnectedServices() == null
                ? new HashMap<>()
                : new HashMap<>(user.getConnectedServices());

        User.StreamingService spotifyService = connectedServices.getOrDefault(
                SPOTIFY_SERVICE,
                User.StreamingService.builder().build());

        spotifyService.setConnected(true);
        spotifyService.setConnectedAt(now);
        spotifyService.setServiceUserId(spotifyProfile.getId());
        spotifyService.setServiceDisplayName(spotifyProfile.getDisplayName());

        connectedServices.put(SPOTIFY_SERVICE, spotifyService);
        user.setConnectedServices(connectedServices);
        user.setUpdatedAt(now);

        userRepository.save(user);
    }

    private SpotifyUserProfileResponse fetchSpotifyProfile(String accessToken) {
        return restClient.get()
                .uri(SPOTIFY_ME_URL)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(SpotifyUserProfileResponse.class);
    }

    private SpotifyTokenResponse exchangeCodeForTokens(String code, String codeVerifier) {
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("grant_type", "authorization_code");
        requestBody.add("code", code);
        requestBody.add("redirect_uri", appProperties.getSpotify().getRedirectUri());
        requestBody.add("client_id", appProperties.getSpotify().getClientId());
        requestBody.add("code_verifier", codeVerifier);

        SpotifyTokenResponse tokenResponse = restClient.post()
                .uri(appProperties.getSpotify().getTokenUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(requestBody)
                .retrieve()
                .body(SpotifyTokenResponse.class);

        if (tokenResponse == null
                || tokenResponse.getAccessToken() == null
                || tokenResponse.getRefreshToken() == null
                || tokenResponse.getExpiresIn() == null) {
            throw new UnauthorizedException("Failed to exchange authorization code for tokens");
        }

        return tokenResponse;
    }

    private String buildSpotifyAuthUrl(String codeChallenge, String state) {
        AppProperties.Spotify spotify = appProperties.getSpotify();

        return spotify.getAuthUrl()
                + "?client_id=" + encode(spotify.getClientId())
                + "&response_type=code"
                + "&redirect_uri=" + encode(spotify.getRedirectUri())
                + "&scope=" + encode(spotify.getScopes())
                + "&code_challenge=" + encode(codeChallenge)
                + "&code_challenge_method=S256"
                + "&state=" + encode(state);
    }

    private String generateCodeVerifier() {
        byte[] randomBytes = new byte[64];
        java.security.SecureRandom secureRandom = new java.security.SecureRandom();
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String generateCodeChallenge(String codeVerifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(codeVerifier.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm is not available", ex);
        }
    }

    private String generateState() {
        byte[] randomBytes = new byte[32];
        java.security.SecureRandom secureRandom = new java.security.SecureRandom();
        secureRandom.nextBytes(randomBytes);
        return HexFormat.of().formatHex(randomBytes);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private void cleanupExpiredPkceSessions() {
        Instant cutoff = Instant.now().minusSeconds(600);
        pkceSessionsByState.entrySet().removeIf(entry -> entry.getValue().createdAt().isBefore(cutoff));
    }

    private record PkceSession(String userId, String codeVerifier, Instant createdAt) {
    }
}
