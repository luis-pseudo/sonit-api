package com.sonit.api.location.service;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Service;

import com.sonit.api.common.exception.ConflictException;
import com.sonit.api.integration.spotify.dto.TrackDto;
import com.sonit.api.integration.spotify.service.SpotifyPlayerService;
import com.sonit.api.location.dto.LocationUpdateRequest;
import com.sonit.api.location.model.LiveLocation;
import com.sonit.api.location.model.TrackInfo;
import com.sonit.api.location.repository.LiveLocationRepository;

@Service
public class LocationService {

    private static final int LOCATION_TTL_MINUTES = 5;

    private final LiveLocationRepository liveLocationRepository;
    private final SpotifyPlayerService spotifyPlayerService;

    public LocationService(LiveLocationRepository liveLocationRepository, SpotifyPlayerService spotifyPlayerService) {
        this.liveLocationRepository = liveLocationRepository;
        this.spotifyPlayerService = spotifyPlayerService;
    }

    public LiveLocation updateLocation(String userId, LocationUpdateRequest request) {
        Instant now = Instant.now();
        TrackInfo currentTrack = resolveCurrentTrack(userId, now);
        LiveLocation liveLocation = liveLocationRepository.findByUserId(userId)
                .orElse(LiveLocation.builder().id(userId).userId(userId).build());

        liveLocation.setId(userId);
        liveLocation.setUserId(userId);
        liveLocation.setLatitude(request.getLatitude());
        liveLocation.setLongitude(request.getLongitude());
        liveLocation.setPosition(new GeoJsonPoint(request.getLongitude(), request.getLatitude()));
        liveLocation.setVisible(request.getVisible() == null || request.getVisible());
        liveLocation.setCurrentTrack(currentTrack);
        liveLocation.setUpdatedAt(now);
        liveLocation.setExpiresAt(now.plusSeconds(LOCATION_TTL_MINUTES * 60L));

        return liveLocationRepository.save(liveLocation);
    }

    private TrackInfo resolveCurrentTrack(String userId, Instant now) {
        try {
            Optional<TrackDto> currentlyPlaying = spotifyPlayerService.getCurrentlyPlaying(userId);
            if (currentlyPlaying.isEmpty()) {
                return null;
            }

            TrackDto track = currentlyPlaying.get();
            return TrackInfo.builder()
                    .trackId(track.getTrackId())
                    .trackName(track.getTrackName())
                    .artistName(track.getArtistName())
                    .albumName(track.getAlbumName())
                    .albumArtUrl(track.getAlbumArtUrl())
                    .progressMs(track.getProgressMs())
                    .durationMs(track.getDurationMs())
                    .playing(track.isPlaying())
                    .updatedAt(now)
                    .build();
        } catch (ConflictException ex) {
            return null;
        }
    }
}
