package com.sonit.api.common.exception;

import org.springframework.http.HttpStatus;

public class SpotifyRateLimitException extends ApiException {

    private final String retryAfter;

    public SpotifyRateLimitException(String retryAfter) {
        super(HttpStatus.TOO_MANY_REQUESTS, "Spotify rate limit exceeded");
        this.retryAfter = retryAfter;
    }

    public String getRetryAfter() {
        return retryAfter;
    }
}
