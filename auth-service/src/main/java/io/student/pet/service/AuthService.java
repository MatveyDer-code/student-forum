package io.student.pet.service;

import io.student.pet.dto.UserRequest;
import io.student.pet.model.Role;
import io.student.pet.model.User;
import io.student.pet.repository.RoleRepository;
import io.student.pet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public User register(UserRequest request) {
        Role role = roleRepository.findByName(request.role())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        User user = new User(request.username(), request.password(), request.email(), role);

        return userRepository.save(user);
    }
}