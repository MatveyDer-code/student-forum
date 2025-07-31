package io.student.pet.service;

import io.student.pet.dto.UserRequest;
import io.student.pet.exception.EmailAlreadyExistsException;
import io.student.pet.exception.RoleNotFoundException;
import io.student.pet.exception.UserNotFoundException;
import io.student.pet.exception.UsernameAlreadyExistsException;
import io.student.pet.model.Role;
import io.student.pet.model.User;
import io.student.pet.repository.RoleRepository;
import io.student.pet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User register(UserRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new UsernameAlreadyExistsException();
        }

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new EmailAlreadyExistsException();
        }

        Role role = roleRepository.findByName(request.role())
                .orElseThrow(RoleNotFoundException::new);

        User user = new User(request.username(), passwordEncoder.encode(request.password()), request.email(), role);

        return userRepository.save(user);
    }


    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(
                () -> new UserNotFoundException("User with username '" + username + "' not found")
        );
    }
}