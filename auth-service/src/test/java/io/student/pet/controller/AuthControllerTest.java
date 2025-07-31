package io.student.pet.controller;

import io.student.pet.model.User;
import io.student.pet.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    MockMvcTester mvcTester;

    @MockitoBean
    AuthService authService;
//    @Test
//    void getExistingUserShouldReturnOk() {
//        User user = new User();
//        user.setUsername("alice");
//        user.setEmail("alice@example.com");
//
//        when(authService.getUserById())
//        assertThat(mvcTester.get().uri("/user/{userId}", ))
//    }
}