package io.student.pet.controller;

import io.student.pet.dto.AuthResponse;
import io.student.pet.dto.LoginRequest;
import io.student.pet.dto.UserRequest;
import io.student.pet.dto.UserResponse;
import io.student.pet.model.User;
import io.student.pet.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@AllArgsConstructor
public class AuthController {
    AuthService authService;

    @GetMapping(value = "/user/{userId}")
    public ResponseEntity<User> getUser(@PathVariable("userId") Long id) {
        User user = authService.getUserById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        AuthResponse token = authService.login(request.username(), request.password());
        return ResponseEntity.ok(Map.of(
                "accessToken", token.accessToken(),
                "refreshToken", token.refreshToken())
        );
    }

    @PostMapping(value = "/register")
    public ResponseEntity<UserResponse> register(@RequestBody @Valid UserRequest request,
                                         HttpServletRequest servletRequest) {
        UserResponse createdUser = authService.register(request);

        String baseUrl = servletRequest.getRequestURL().toString().replace(servletRequest.getRequestURI(), "");
        URI location = URI.create(baseUrl + "/user/" + createdUser.id());
        return ResponseEntity.created(location).body(createdUser);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        AuthResponse newTokens = authService.refreshAccessToken(refreshToken);

        return ResponseEntity.ok(newTokens);
    }
}