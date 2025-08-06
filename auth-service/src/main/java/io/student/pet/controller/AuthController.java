package io.student.pet.controller;

import io.student.pet.dto.LoginRequest;
import io.student.pet.dto.UserRequest;
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
        String token = authService.login(request.username(), request.password());
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping(value = "/register")
    public ResponseEntity<User> register(@RequestBody @Valid UserRequest request,
                                         HttpServletRequest servletRequest) {
        User createdUser = authService.register(request);
        String baseUrl = servletRequest.getRequestURL().toString().replace(servletRequest.getRequestURI(), "");
        URI location = URI.create(baseUrl + "/user/" + createdUser.getId());
        return ResponseEntity.created(location).body(createdUser);
    }
}