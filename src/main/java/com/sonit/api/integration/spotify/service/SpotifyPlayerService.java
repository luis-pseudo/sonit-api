package com.sonit.api.integration.spotify.service;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.sonit.api.common.exception.ApiException;
import com.sonit.api.common.exception.SpotifyRateLimitException;
import com.sonit.api.common.exception.UnauthorizedException;
import com.sonit.api.integration.spotify.dto.SpotifyCurrentlyPlayingResponse;
import com.sonit.api.integration.spotify.dto.TrackDto;

@Service
public class SpotifyPlayerService {

    private static final String SPOTIFY_CURRENTLY_PLAYING_URL = "https://api.spotify.com/v1/me/player/currently-playing";

    private final SpotifyTokenService spotifyTokenService;
    private final RestClient restClient;

    public SpotifyPlayerService(SpotifyTokenService spotifyTokenService) {
        this.spotifyTokenService = spotifyTokenService;
        this.restClient = RestClient.builder().build();
    }

    public Optional<TrackDto> getCurrentlyPlaying(String userId) {
        String accessToken = spotifyTokenService.getValidAccessToken(userId);

        for (int attempt = 0; attempt < 2; attempt++) {
            try {
                return fetchCurrentlyPlaying(accessToken);
            } catch (UnauthorizedException ex) {
                if (attempt == 1) {
                    throw ex;
                }
                accessToken = spotifyTokenService.refreshAccessToken(userId);
            }
        }

        return Optional.empty();
    }

    private Optional<TrackDto> fetchCurrentlyPlaying(String accessToken) {
        return restClient.get()
                .uri(SPOTIFY_CURRENTLY_PLAYING_URL)
                .header("Authorization", "Bearer " + accessToken)
                .exchange((request, response) -> handleCurrentlyPlayingResponse(response));
    }

    private Optional<TrackDto> handleCurrentlyPlayingResponse(
            RestClient.RequestHeadersSpec.ConvertibleClientHttpResponse response) throws java.io.IOException {
        HttpStatusCode statusCode = response.getStatusCode();
        int status = statusCode.value();

        if (status == HttpStatus.NO_CONTENT.value()) {
            return Optional.empty();
        }

        if (status == HttpStatus.UNAUTHORIZED.value()) {
            throw new UnauthorizedException("Spotify access token expired or invalid");
        }

        if (status == HttpStatus.TOO_MANY_REQUESTS.value()) {
            String retryAfter = response.getHeaders().getFirst("Retry-After");
            throw new SpotifyRateLimitException(retryAfter != null ? retryAfter : "1");
        }

        if (status != HttpStatus.OK.value()) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Failed to fetch Spotify currently playing track");
        }

        SpotifyCurrentlyPlayingResponse currentlyPlayingResponse =
                response.bodyTo(SpotifyCurrentlyPlayingResponse.class);

        if (currentlyPlayingResponse == null
                || !currentlyPlayingResponse.isPlaying()
                || currentlyPlayingResponse.getItem() == null) {
            return Optional.empty();
        }

        return Optional.of(mapToTrackDto(currentlyPlayingResponse));
    }

    private TrackDto mapToTrackDto(SpotifyCurrentlyPlayingResponse response) {
        SpotifyCurrentlyPlayingResponse.Item item = response.getItem();
        String artistName = item.getArtists() == null || item.getArtists().isEmpty()
                ? null
                : item.getArtists().get(0).getName();
        String albumArtUrl = item.getAlbum() == null
                || item.getAlbum().getImages() == null
                || item.getAlbum().getImages().isEmpty()
                ? null
                : item.getAlbum().getImages().get(0).getUrl();

        return TrackDto.builder()
                .trackId(item.getId())
                .trackName(item.getName())
                .artistName(artistName)
                .albumName(item.getAlbum() != null ? item.getAlbum().getName() : null)
                .albumArtUrl(albumArtUrl)
                .progressMs(response.getProgressMs() != null ? response.getProgressMs() : 0)
                .durationMs(item.getDurationMs() != null ? item.getDurationMs() : 0)
                .isPlaying(response.isPlaying())
                .build();
    }
}
