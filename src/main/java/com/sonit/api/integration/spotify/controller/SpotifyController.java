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
import com.sonit.api.integration.spotify.dto.TrackDto;
import com.sonit.api.integration.spotify.service.SpotifyAuthService;
import com.sonit.api.integration.spotify.service.SpotifyPlayerService;

@RestController
@RequestMapping("/integrations/spotify")
public class SpotifyController {

    private static final URI SPOTIFY_CONNECTED_DEEP_LINK = URI.create("sonit://spotify-connected");

    private final SpotifyAuthService spotifyAuthService;
    private final SpotifyPlayerService spotifyPlayerService;

    public SpotifyController(SpotifyAuthService spotifyAuthService, SpotifyPlayerService spotifyPlayerService) {
        this.spotifyAuthService = spotifyAuthService;
        this.spotifyPlayerService = spotifyPlayerService;
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

        @GetMapping("/currently-playing")
        public ResponseEntity<ApiResponse<TrackDto>> getCurrentlyPlaying() {
        String userId = SecurityUtils.getCurrentUserId();
        return spotifyPlayerService.getCurrentlyPlaying(userId)
            .<ResponseEntity<ApiResponse<TrackDto>>>map(track -> ResponseEntity.ok(
                ApiResponse.<TrackDto>success("Currently playing track fetched successfully", track)))
            .orElseGet(() -> ResponseEntity.ok(ApiResponse.<TrackDto>success("Sin reproducción activa", null)));
        }
}
