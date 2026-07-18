package com.example.Ecomerce.security;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET =
            "c2VjcmV0LWtleS1wYXJhLWRlc2Vudm9sdmltZW50by10cm9jYXItZW0tcHJvZHVjYW8tMTIzNDU2Nzg=";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 3600_000L);
    }

    private UserDetails userDetails(String username) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(username)
                .password("irrelevant")
                .authorities("ROLE_CUSTOMER")
                .build();
    }

    @Test
    void generateToken_andExtractUsername_roundTrips() {
        UserDetails user = userDetails("cliente@example.com");

        String token = jwtService.generateToken(user);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("cliente@example.com");
    }

    @Test
    void isTokenValid_trueForMatchingUserAndUnexpiredToken() {
        UserDetails user = userDetails("cliente@example.com");
        String token = jwtService.generateToken(user);

        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void isTokenValid_falseForDifferentUser() {
        UserDetails user = userDetails("cliente@example.com");
        UserDetails otherUser = userDetails("outro@example.com");
        String token = jwtService.generateToken(user);

        assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
    }

    @Test
    void expiredToken_throwsOnValidation() {
        JwtService shortLivedJwtService = new JwtService(SECRET, -1000L);
        UserDetails user = userDetails("cliente@example.com");
        String expiredToken = shortLivedJwtService.generateToken(user);

        assertThatThrownBy(() -> shortLivedJwtService.isTokenValid(expiredToken, user))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
