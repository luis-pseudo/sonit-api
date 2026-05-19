package com.sonit.api.config;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.mock.env.MockEnvironment;

class AppPropertiesTest {

    @Test
    void shouldBindAppPropertiesFromEnvironment() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("app.jwt.secret", "secret")
                .withProperty("app.jwt.expiration", "3600000")
                .withProperty("app.jwt.refresh-expiration", "7200000")
                .withProperty("app.spotify.client-id", "client-id")
                .withProperty("app.spotify.client-secret", "client-secret")
                .withProperty("app.spotify.auth-url", "https://accounts.spotify.com/authorize")
                .withProperty("app.spotify.token-url", "https://accounts.spotify.com/api/token")
                .withProperty("app.spotify.redirect-uri", "http://localhost:8080/callback")
                .withProperty("app.spotify.scopes", "user-read-currently-playing,user-top-read")
                .withProperty("app.cors.allowed-origins[0]", "http://localhost:3000")
                .withProperty("app.cors.allowed-origins[1]", "http://localhost:8080");

        AppProperties appProperties = Binder.get(environment)
                .bind("app", Bindable.of(AppProperties.class))
                .orElseThrow(null);

        assertThat(appProperties.getJwt().getSecret()).isEqualTo("secret");
        assertThat(appProperties.getJwt().getExpiration()).isEqualTo(3600000L);
        assertThat(appProperties.getJwt().getRefreshExpiration()).isEqualTo(7200000L);
        assertThat(appProperties.getSpotify().getClientId()).isEqualTo("client-id");
        assertThat(appProperties.getSpotify().getClientSecret()).isEqualTo("client-secret");
        assertThat(appProperties.getSpotify().getAuthUrl()).isEqualTo("https://accounts.spotify.com/authorize");
        assertThat(appProperties.getSpotify().getTokenUrl()).isEqualTo("https://accounts.spotify.com/api/token");
        assertThat(appProperties.getSpotify().getRedirectUri()).isEqualTo("http://localhost:8080/callback");
        assertThat(appProperties.getSpotify().getScopes()).isEqualTo("user-read-currently-playing,user-top-read");
        assertThat(appProperties.getCors().getAllowedOrigins()).containsExactly("http://localhost:3000", "http://localhost:8080");
    }
}
