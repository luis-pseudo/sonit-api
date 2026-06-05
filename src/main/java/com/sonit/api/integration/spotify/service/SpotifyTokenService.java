package com.sonit.api.integration.spotify.service;

import java.time.Instant;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.sonit.api.common.exception.ConflictException;
import com.sonit.api.common.exception.UnauthorizedException;
import com.sonit.api.config.AppProperties;
import com.sonit.api.integration.spotify.dto.SpotifyTokenResponse;
import com.sonit.api.user.model.UserTokens;
import com.sonit.api.user.repository.UserTokensRepository;

@Service
public class SpotifyTokenService {

    private static final String SPOTIFY_SERVICE = "spotify";
    private static final long REFRESH_THRESHOLD_SECONDS = 300;

    private final AppProperties appProperties;
    private final UserTokensRepository userTokensRepository;
    private final RestClient restClient;

    public SpotifyTokenService(AppProperties appProperties, UserTokensRepository userTokensRepository) {
        this.appProperties = appProperties;
        this.userTokensRepository = userTokensRepository;
        this.restClient = RestClient.builder().build();
    }

    public String getValidAccessToken(String userId) {
        UserTokens userTokens = getUserSpotifyTokens(userId);
        Instant now = Instant.now();

        if (userTokens.getExpiresAt() != null && userTokens.getExpiresAt().isAfter(now.plusSeconds(REFRESH_THRESHOLD_SECONDS))) {
            return userTokens.getAccessToken();
        }

        return refreshAccessToken(userId);
    }

    public String refreshAccessToken(String userId) {
        UserTokens userTokens = getUserSpotifyTokens(userId);
        if (userTokens.getRefreshToken() == null || userTokens.getRefreshToken().isBlank()) {
            throw new UnauthorizedException("Spotify refresh token is missing");
        }

        SpotifyTokenResponse tokenResponse = exchangeRefreshToken(userTokens.getRefreshToken());
        Instant now = Instant.now();

        userTokens.setAccessToken(tokenResponse.getAccessToken());
        if (tokenResponse.getRefreshToken() != null && !tokenResponse.getRefreshToken().isBlank()) {
            userTokens.setRefreshToken(tokenResponse.getRefreshToken());
        }
        userTokens.setExpiresAt(now.plusSeconds(tokenResponse.getExpiresIn()));
        userTokensRepository.save(userTokens);

        return userTokens.getAccessToken();
    }

    private UserTokens getUserSpotifyTokens(String userId) {
        return userTokensRepository.findByUserIdAndService(userId, SPOTIFY_SERVICE)
                .orElseThrow(() -> new ConflictException("Spotify no conectado"));
    }

    private SpotifyTokenResponse exchangeRefreshToken(String refreshToken) {
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("grant_type", "refresh_token");
        requestBody.add("refresh_token", refreshToken);
        requestBody.add("client_id", appProperties.getSpotify().getClientId());
        requestBody.add("client_secret", appProperties.getSpotify().getClientSecret());

        try {
            SpotifyTokenResponse tokenResponse = restClient.post()
                    .uri(appProperties.getSpotify().getTokenUrl())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(requestBody)
                    .retrieve()
                    .body(SpotifyTokenResponse.class);

            if (tokenResponse == null || tokenResponse.getAccessToken() == null || tokenResponse.getExpiresIn() == null) {
                throw new UnauthorizedException("Failed to refresh Spotify access token");
            }

            return tokenResponse;
        } catch (RestClientResponseException ex) {
            throw new UnauthorizedException("Failed to refresh Spotify access token");
        }
    }
}
