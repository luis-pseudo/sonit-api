package com.sonit.api.user.dto;

import java.time.Instant;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {

    private String id;
    private String displayName;
    private String username;
    private String email;
    private String photoUrl;
    private Map<String, ConnectedServiceDto> connectedServices;
    private boolean visible;
    private Instant createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConnectedServiceDto {
        private boolean connected;
    }
}
