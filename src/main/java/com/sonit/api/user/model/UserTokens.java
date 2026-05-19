package com.sonit.api.user.model;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_tokens")
public class UserTokens {

    @Id
    private String id;
    private String userId;
    private String service;
    private String accessToken;
    private String refreshToken;
    private Instant expiresAt;
}
