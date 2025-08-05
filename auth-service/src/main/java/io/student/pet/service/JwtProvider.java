package io.student.pet.service;

import io.student.pet.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtProvider {
    public String generateToken(User user) {
        return "dummy-jwt-token-for-" + user.getUsername();
    }
}