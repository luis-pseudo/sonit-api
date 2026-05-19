package com.sonit.api.location.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NearbyLocationDto {
    private String userId;
    private Double latitude;
    private Double longitude;
    private Double distanceMeters;
    private TrackInfoDto currentTrack;
}
