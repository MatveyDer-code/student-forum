package io.student.pet.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "jwt.secret=verySecretKeyForJwtGeneration1234567890",
        "jwt.expiration=3600000"
})
public class AuthControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }


    @Test
    void shouldRegisterUserSuccessfully() {
        String url = getBaseUrl() + "/register";

        String json = """
            {
                "username": "integrationUser",
                "password": "StrongP@ss1",
                "email": "integration@example.com",
                "role": "STUDENT"
            }
        """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(json, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).contains("integrationUser");
    }

    @Test
    void shouldAccessProtectedEndpointWithValidToken() {
        String url = getBaseUrl() + "/login";

        String json = """
        {
            "username": "alice",
            "password": "password1"
        }
    """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(json, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("token");

        String token = response.getBody()
                .replace("{\"token\":\"", "")
                .replace("\"}", "");

        String protectedUrl = getBaseUrl() + "/user/1";

        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> protectedResponse = restTemplate.exchange(
                protectedUrl,
                HttpMethod.GET,
                entity,
                String.class
        );

        assertThat(protectedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(protectedResponse.getBody()).contains("alice");
    }

    @Test
    void shouldReturnUnauthorizedForInvalidCredentials() {
        String url = getBaseUrl() + "/login";

        String json = """
        {
            "username": "alice",
            "password": "WrongPassword!"
        }
    """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(json, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).contains("Invalid username or password");
    }

    @Test
    void shouldReturnUnauthorizedForInvalidJwtToken() {
        String protectedUrl = getBaseUrl() + "/user/1";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("invalid.jwt.token.here");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                protectedUrl,
                HttpMethod.GET,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturnUnauthorizedForExpiredJwtToken() {
        String url = getBaseUrl() + "/login";

        String json = """
        {
            "username": "alice",
            "password": "password1"
        }
    """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(json, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("token");

        String secret = "verySecretKeyForJwtGeneration1234567890";

        String expiredToken = Jwts.builder()
                .setSubject("alice")
                .setIssuedAt(new Date(System.currentTimeMillis() - 3600_000))
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS256)
                .compact();

        String protectedUrl = getBaseUrl() + "/user/1";
        headers.setBearerAuth(expiredToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> protectedResponse = restTemplate.exchange(
                protectedUrl,
                HttpMethod.GET,
                entity,
                String.class
        );

        assertThat(protectedResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}