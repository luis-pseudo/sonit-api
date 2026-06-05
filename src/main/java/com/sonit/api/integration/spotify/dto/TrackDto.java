package com.sonit.api.integration.spotify.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackDto {

    private String trackId;
    private String trackName;
    private String artistName;
    private String albumName;
    private String albumArtUrl;
    private int progressMs;
    private int durationMs;

    @JsonProperty("isPlaying")
    private boolean isPlaying;
}
