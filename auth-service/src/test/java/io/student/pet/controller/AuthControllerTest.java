package io.student.pet.controller;

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
    void getExistingUserShouldReturnOk() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setEmail("alice@example.com");

        when(authService.getUserById(1L)).thenReturn(user);
        assertThat(mvcTester.get().uri("/user/{userId}", user.getId())
                .accept(MediaType.APPLICATION_JSON))
                .hasStatus(HttpStatus.OK);
    }
}