package com.sonit.api.location.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackInfoDto {
    private String trackId;
    private String trackName;
    private String artistName;
    private String albumName;
    private String albumArtUrl;
    private Integer progressMs;
    private Integer durationMs;
    private boolean playing;
    private Instant updatedAt;
}
