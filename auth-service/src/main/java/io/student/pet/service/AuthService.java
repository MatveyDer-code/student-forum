package io.student.pet.service;
import lombok.extern.slf4j.Slf4j;

import io.student.pet.dto.AuthResponse;
import io.student.pet.dto.UserRequest;
import io.student.pet.dto.UserResponse;
import io.student.pet.exception.AuthenticationException;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtProvider jwtProvider;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserResponse register(UserRequest request) {
        log.info("Попытка регистрации пользователя: {}", request.username());
        if (userRepository.findByUsername(request.username()).isPresent()) {
            log.warn("Регистрация не удалась: имя пользователя '{}' уже занято", request.username());
            throw new UsernameAlreadyExistsException();
        }

        if (userRepository.findByEmail(request.email()).isPresent()) {
            log.warn("Регистрация не удалась: email '{}' уже занят", request.email());
            throw new EmailAlreadyExistsException();
        }

        Role role = roleRepository.findByName(request.role())
                .orElseThrow(RoleNotFoundException::new);

        User user = new User(
                request.username(),
                passwordEncoder.encode(request.password()),
                request.email(),
                role
        );

        User savedUser = userRepository.save(user);

        log.info("Пользователь '{}' успешно зарегистрирован с ролью '{}'", savedUser.getUsername(), savedUser.getRole().getName());
        return new UserResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRole().getName()
        );
    }

    public AuthResponse login(String username, String rawPassword) {
        log.info("Попытка входа пользователя: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Вход не удался: пользователь с именем '{}' не найден", username);
                    return new UserNotFoundException("User with username '" + username + "' not found");
                });

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            log.warn("Вход не удался: неверный пароль для пользователя '{}'", username);
            throw new AuthenticationException();
        }

        log.info("Пользователь '{}' успешно вошел в систему", username);
        return new AuthResponse(jwtProvider.generateAccessToken(user), jwtProvider.generateRefreshToken(user));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(
                () -> new UserNotFoundException("User with username '" + username + "' not found")
        );
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole() != null ? user.getRole().getName() : null
        );
    }

    public AuthResponse refreshAccessToken(String refreshToken) {
        log.debug("Обработка запроса обновления токена");
        if (jwtProvider.validateRefreshToken(refreshToken)) {
            String username = jwtProvider.getUsernameFromToken(refreshToken);
            User user = this.findByUsername(username);
            String newAccessToken = jwtProvider.generateAccessToken(user);
            log.info("Токен доступа успешно обновлен для пользователя '{}'", username);
            return new AuthResponse(newAccessToken, refreshToken);
        }

        log.warn("Токен обновления недействителен");
        throw new AuthenticationException();
    }
}