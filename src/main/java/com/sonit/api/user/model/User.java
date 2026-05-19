package com.sonit.api.user.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;
    private String displayName;
    private String photoUrl;

    @Indexed(unique = true, sparse = true)
    private String email;
    private String passwordHash;

    @Indexed(unique = true, sparse = true)
    private String phoneNumber;

    private List<AuthProvider> authProviders;
    private Map<String, StreamingService> connectedServices;

    private boolean active;
    private boolean visible;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastSeenAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthProvider {
        private String provider;
        private String providerId;
        private String email;
        private Instant linkedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StreamingService {
        private boolean connected;
        private String serviceUserId;
        private String serviceDisplayName;
        private Instant connectedAt;
        private Instant tokenExpiresAt;
    }
}
