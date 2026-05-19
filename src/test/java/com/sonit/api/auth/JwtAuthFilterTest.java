package com.sonit.api.auth;

import com.sonit.api.auth.service.JwtService;
import com.sonit.api.config.AppProperties;
import com.sonit.api.user.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthFilterTest {

    private static final String TEST_SECRET = "test-secret-which-is-very-long-and-secure-for-hs512-signing-key-size";

    private JwtAuthFilter jwtAuthFilter;
    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        AppProperties properties = new AppProperties();
        AppProperties.Jwt jwtProperties = new AppProperties.Jwt();
        jwtProperties.setSecret(TEST_SECRET);
        jwtProperties.setExpiration(3600000L);
        jwtProperties.setRefreshExpiration(7200000L);
        properties.setJwt(jwtProperties);

        jwtService = new JwtService(properties);
        jwtAuthFilter = new JwtAuthFilter(jwtService);
        user = User.builder().id("user-123").build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticateRequestWhenBearerAccessTokenIsValid() throws Exception {
        String accessToken = jwtService.generateAccessToken(user);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + accessToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        jwtAuthFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("user-123");
    }

    @Test
    void shouldNotAuthenticateWhenTokenIsRefreshToken() throws Exception {
        String refreshToken = jwtService.generateRefreshToken(user);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + refreshToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        jwtAuthFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void shouldNotAuthenticateWhenAuthorizationHeaderIsMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        jwtAuthFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
