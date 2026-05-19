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
public class SpotifyUserProfileResponse {

    private String id;

    @JsonProperty("display_name")
    private String displayName;
}
