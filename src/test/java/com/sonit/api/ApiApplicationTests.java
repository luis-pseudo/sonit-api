package com.sonit.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.data.mongodb.uri=mongodb://localhost:27017/sonit-test",
		"app.jwt.secret=test-secret",
		"app.jwt.expiration=3600000",
		"app.jwt.refresh-expiration=7200000",
		"app.spotify.client-id=test-client-id",
		"app.spotify.client-secret=test-client-secret",
		"app.spotify.auth-url=https://accounts.spotify.com/authorize",
		"app.spotify.token-url=https://accounts.spotify.com/api/token",
		"app.spotify.redirect-uri=http://localhost:8080/api/integrations/spotify/callback",
		"app.spotify.scopes=user-read-currently-playing",
		"app.cors.allowed-origins[0]=http://localhost:3000"
})
class ApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
