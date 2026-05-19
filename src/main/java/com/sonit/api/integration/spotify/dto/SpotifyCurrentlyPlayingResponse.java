package com.sonit.api.integration.spotify.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpotifyCurrentlyPlayingResponse {

    @JsonProperty("is_playing")
    private boolean playing;

    @JsonProperty("progress_ms")
    private Integer progressMs;

    private Item item;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private String id;
        private String name;
        private List<Artist> artists;
        private Album album;

        @JsonProperty("duration_ms")
        private Integer durationMs;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Artist {
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Album {
        private String name;
        private List<Image> images;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Image {
        private String url;
    }
}
