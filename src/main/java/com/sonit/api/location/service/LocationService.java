package com.sonit.api.location.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Service;

import com.sonit.api.common.exception.ConflictException;
import com.sonit.api.common.exception.UnauthorizedException;
import com.sonit.api.integration.spotify.dto.TrackDto;
import com.sonit.api.integration.spotify.service.SpotifyPlayerService;
import com.sonit.api.location.dto.LocationUpdateRequest;
import com.sonit.api.location.dto.NearbyLocationDto;
import com.sonit.api.location.dto.TrackInfoDto;
import com.sonit.api.location.model.LiveLocation;
import com.sonit.api.location.model.TrackInfo;
import com.sonit.api.location.repository.LiveLocationRepository;
import com.sonit.api.user.model.User;
import com.sonit.api.user.repository.UserRepository;

@Service
public class LocationService {

    private static final int LOCATION_TTL_MINUTES = 5;

    private final LiveLocationRepository liveLocationRepository;
    private final SpotifyPlayerService spotifyPlayerService;
    private final UserRepository userRepository;

    public LocationService(LiveLocationRepository liveLocationRepository, SpotifyPlayerService spotifyPlayerService, UserRepository userRepository) {
        this.liveLocationRepository = liveLocationRepository;
        this.spotifyPlayerService = spotifyPlayerService;
        this.userRepository = userRepository;
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
        } catch (ConflictException | UnauthorizedException ex) {
            // Spotify no conectado o token inválido — continuar sin canción
            return null;
        } catch (Exception ex) {
            // Cualquier otro error de Spotify no debe bloquear la ubicación
            return null;
        }
    }

    public List<NearbyLocationDto> findNearby(String requestingUserId, double latitude, double longitude, double radiusMeters, int limit) {
        Point point = new Point(longitude, latitude);
        Distance distance = new Distance(radiusMeters / 1000.0, Metrics.KILOMETERS);
        Pageable pageable = PageRequest.of(0, Math.max(1, Math.min(200, limit)));
        List<LiveLocation> nearby = liveLocationRepository.findByVisibleTrueAndPositionNear(point, distance, pageable);
        List<LiveLocation> filtered = nearby.stream().filter(loc -> !loc.getUserId().equals(requestingUserId)).toList();

        List<String> userIds = filtered.stream().map(LiveLocation::getUserId).toList();

        Map<String, User> usersById = userRepository.findAllById(userIds).stream().collect(Collectors.toMap(User::getId, u -> u));

        return filtered.stream().map(loc -> {
                    User user = usersById.get(loc.getUserId());
                    return NearbyLocationDto.builder()
                            .userId(loc.getUserId())
                            .username(user != null ? user.getUsername() : null)
                            .displayName(user != null ? user.getDisplayName() : null)
                            .photoUrl(user != null ? user.getPhotoUrl() : null)
                            .latitude(loc.getLatitude())
                            .longitude(loc.getLongitude())
                            .distanceMeters(computeDistanceMeters(
                                    latitude, longitude,
                                    loc.getLatitude(), loc.getLongitude()))
                            .currentTrack(mapTrackInfo(loc.getCurrentTrack()))
                            .build();
                }).collect(Collectors.toList());
    }

    private static TrackInfoDto mapTrackInfo(TrackInfo t) {
        if (t == null) return null;
        return TrackInfoDto.builder()
                .trackId(t.getTrackId())
                .trackName(t.getTrackName())
                .artistName(t.getArtistName())
                .albumName(t.getAlbumName())
                .albumArtUrl(t.getAlbumArtUrl())
                .progressMs(t.getProgressMs())
                .durationMs(t.getDurationMs())
                .playing(t.isPlaying())
                .updatedAt(t.getUpdatedAt())
                .build();
    }

    // Haversine formula to compute meters between two lat/lng points
    private static double computeDistanceMeters(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Earth radius in meters
        double latRad1 = Math.toRadians(lat1);
        double latRad2 = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(latRad1) * Math.cos(latRad2)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
