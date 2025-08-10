package io.student.pet.service;

import io.student.pet.exception.InvalidJwtTokenException;
import io.student.pet.model.Role;
import io.student.pet.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtProviderTest {

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        String secret = "verySecretKeyForJwtGeneration12345";
        long accessExpiration = 3600000;
        long refreshExpiration = 604800000;
        jwtProvider = new JwtProvider(secret, accessExpiration, refreshExpiration);
    }

    @Test
    void generateAccessTokenShouldReturnValidJwt() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        Role role = new Role();
        role.setId(1L);
        role.setName("USER");
        user.setRole(role);

        String token = jwtProvider.generateAccessToken(user);

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void validateAccessTokenShouldReturnTrueForValidToken() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@mail.ru");
        user.setUsername("test");
        Role role = new Role();
        role.setId(1L);
        role.setName("USER");
        user.setRole(role);

        String token = jwtProvider.generateAccessToken(user);
        assertThat(jwtProvider.validateAccessToken(token)).isTrue();
    }

    @Test
    void validateAccessTokenShouldReturnFalseForInvalidToken() {
        String invalidToken = "invalid.token.value";

        assertThrows(InvalidJwtTokenException.class, () -> {
            jwtProvider.validateAccessToken(invalidToken);
        });
    }

    @Test
    void generateRefreshTokenShouldBeValidAndHaveCorrectExpiration() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        Role role = new Role();
        role.setId(1L);
        role.setName("USER");
        user.setRole(role);

        String refreshToken = jwtProvider.generateRefreshToken(user);

        assertThat(refreshToken).isNotBlank();
        assertThat(refreshToken.split("\\.")).hasSize(3);
        assertThat(jwtProvider.validateRefreshToken(refreshToken)).isTrue();
        String username = jwtProvider.getUsernameFromToken(refreshToken);
        assertThat(username).isEqualTo("testuser");
    }
}