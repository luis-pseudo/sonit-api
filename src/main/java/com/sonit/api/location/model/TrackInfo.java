package com.sonit.api.location.model;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackInfo {

    private String trackId;
    private String trackName;
    private String artistName;
    private String albumName;
    private String albumArtUrl;
    private int progressMs;
    private int durationMs;
    private boolean playing;
    private Instant updatedAt;
}
