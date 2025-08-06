package io.student.pet.service;

import io.student.pet.model.Role;
import io.student.pet.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class JwtProviderTest {

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        String secret = "verySecretKeyForJwtGeneration12345";
        long expiration = 3600000;
        jwtProvider = new JwtProvider(secret, expiration);
    }

    @Test
    void generateTokenShouldReturnValidJwt() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        Role role = new Role();
        role.setId(1L);
        role.setName("USER");
        user.setRole(role);

        String token = jwtProvider.generateToken(user);

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }
}