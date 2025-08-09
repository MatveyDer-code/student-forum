package io.student.pet.controller;

import io.student.pet.dto.AuthResponse;
import io.student.pet.dto.UserRequest;
import io.student.pet.model.User;
import io.student.pet.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)

class AuthControllerTest {

    @Autowired
    MockMvcTester mvcTester;

    @MockitoBean
    AuthService authService;

    @Test
    void loginUserShouldReturnOk() {
        String accessToken = "dummy-access-token";
        String refreshToken = "dummy-refresh-token";

        when(authService.login("alice", "StrongP@ss1")).thenReturn(new AuthResponse(accessToken, refreshToken));

        assertThat(mvcTester.post().uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                    "username": "alice",
                    "password": "StrongP@ss1"
                }
            """)
                .accept(MediaType.APPLICATION_JSON))
                .hasStatus(HttpStatus.OK)
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson().satisfies(json -> {
                    json.assertThat().extractingPath("$.accessToken").isEqualTo(accessToken);
                    json.assertThat().extractingPath("$.refreshToken").isEqualTo(refreshToken);
                });
    }

    @Test
    void getExistingUserShouldReturnOk() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setEmail("alice@example.com");

        when(authService.getUserById(1L)).thenReturn(user);
        assertThat(mvcTester.get().uri("/user/{userId}", user.getId())
                .accept(MediaType.APPLICATION_JSON))
                .hasStatus(HttpStatus.OK)
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson().satisfies(jsonContent -> {
                    jsonContent.assertThat().extractingPath("$.id").isEqualTo(1);
                    jsonContent.assertThat().extractingPath("$.username").isEqualTo("alice");
                    jsonContent.assertThat().extractingPath("$.email").isEqualTo("alice@example.com");
                });
    }

    @Test
    void registerUserShouldReturnCreated() {
        UserRequest request = new UserRequest("newUser", "StrongP@ss1", "new@example.com", "STUDENT");

        User savedUser = new User();
        savedUser.setId(50L);
        savedUser.setUsername("newUser");
        savedUser.setEmail("new@example.com");

        when(authService.register(request)).thenReturn(savedUser);

        assertThat(mvcTester.post().uri("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                    "username": "newUser",
                    "password": "StrongP@ss1",
                    "email": "new@example.com",
                    "role": "STUDENT"
                }
            """)
                .accept(MediaType.APPLICATION_JSON))
                .hasStatus(HttpStatus.CREATED)
                .hasHeader("Location", "http://localhost/user/50")
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson().satisfies(json -> {
                    json.assertThat().extractingPath("$.id").isEqualTo(50);
                    json.assertThat().extractingPath("$.username").isEqualTo("newUser");
                    json.assertThat().extractingPath("$.email").isEqualTo("new@example.com");
                });
    }
}