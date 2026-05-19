package com.sonit.api.integration.spotify.controller;

import java.net.URI;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sonit.api.common.dto.ApiResponse;
import com.sonit.api.common.util.SecurityUtils;
import com.sonit.api.integration.spotify.dto.SpotifyAuthUrlResponse;
import com.sonit.api.integration.spotify.service.SpotifyAuthService;

@RestController
@RequestMapping("/integrations/spotify")
public class SpotifyController {

    private static final URI SPOTIFY_CONNECTED_DEEP_LINK = URI.create("sonit://spotify-connected");

    private final SpotifyAuthService spotifyAuthService;

    public SpotifyController(SpotifyAuthService spotifyAuthService) {
        this.spotifyAuthService = spotifyAuthService;
    }

    @GetMapping("/auth-url")
    public ResponseEntity<ApiResponse<SpotifyAuthUrlResponse>> getAuthorizationUrl() {
        String userId = SecurityUtils.getCurrentUserId();
        String authUrl = spotifyAuthService.generateAuthUrl(userId);
        SpotifyAuthUrlResponse data = SpotifyAuthUrlResponse.builder().authUrl(authUrl).build();
        return ResponseEntity.ok(ApiResponse.success("Spotify authorization URL generated", data));
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> handleCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String state) {
        spotifyAuthService.handleCallback(code, state);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, SPOTIFY_CONNECTED_DEEP_LINK.toString())
                .build();
    }
}
