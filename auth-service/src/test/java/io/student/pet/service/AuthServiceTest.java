package io.student.pet.service;

import io.student.pet.dto.AuthResponse;
import io.student.pet.dto.UserResponse;
import io.student.pet.exception.AuthenticationException;
import io.student.pet.exception.RoleNotFoundException;
import io.student.pet.exception.UserNotFoundException;
import io.student.pet.model.Role;
import io.student.pet.model.User;
import io.student.pet.repository.RoleRepository;
import io.student.pet.repository.UserRepository;
import io.student.pet.dto.UserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private AuthService authService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode("StrongP@ss1");
        existingUser = new User("alice", hashedPassword, "alice@example.com", new Role("STUDENT"));
        existingUser.setId(1L);
    }

    @Test
    void registerShouldCreateUserWhenRoleExists() {
        Role studentRole = new Role();
        studentRole.setName("STUDENT");

        UserRequest request = new UserRequest("alice", "StrongP@ss1", "alice@example.com", studentRole.getName());

        when(roleRepository.findByName("STUDENT")).thenReturn(Optional.of(studentRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        UserResponse result = authService.register(request);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.username()).isEqualTo("alice");
        assertThat(result.role()).isEqualTo("STUDENT");
    }

    @Test
    void registerShouldThrowExceptionWhenRoleNotExists() {
        Role studentRole = new Role();
        studentRole.setName("NO ROLE");

        UserRequest request = new UserRequest("alice", "StrongP@ss1", "alice@example.com", studentRole.getName());

        when(roleRepository.findByName(studentRole.getName())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RoleNotFoundException.class, () -> {
            authService.register(request);
        });

        assertEquals("Role not found", exception.getMessage());
    }

    @Test
    void shouldReturnUserByNameAndByIdWhenUserExists() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(existingUser));
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        User userByName = authService.findByUsername("alice");

        assertNotNull(userByName);
        assertEquals("alice", userByName.getUsername());

        UserResponse userById = authService.getUserById(1L);
        assertNotNull(userById);
        assertEquals(1L, userById.id());
    }

    @Test
    void shouldThrowUserNotFoundWhenUserNotExistOrNoSuchId() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        when(userRepository.findById(9999L)).thenReturn(Optional.empty());


        RuntimeException nameException = assertThrows(UserNotFoundException.class, () -> {
            authService.findByUsername("unknown");
        });

        assertEquals("User with username 'unknown' not found", nameException.getMessage());

        RuntimeException idException = assertThrows(UserNotFoundException.class, () -> {
            authService.getUserById(9999L);
        });

        assertEquals("User with id 9999 not found", idException.getMessage());
    }

    @Test
    void loginShouldReturnJwtTokenWhenCredentialsValid() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(existingUser));
        when(jwtProvider.generateAccessToken(existingUser)).thenReturn("dummy-jwt-token");
        when(jwtProvider.generateRefreshToken(existingUser)).thenReturn("dummy-refresh-token");

        AuthResponse authResponse = authService.login("alice", "StrongP@ss1");

        assertNotNull(authResponse);
        assertEquals("dummy-jwt-token", authResponse.accessToken());
        assertEquals("dummy-refresh-token", authResponse.refreshToken());

        verify(userRepository, times(1)).findByUsername("alice");
        verify(jwtProvider, times(1)).generateAccessToken(existingUser);
        verify(jwtProvider, times(1)).generateRefreshToken(existingUser);
    }

    @Test
    void loginShouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        when(userRepository.findByUsername("bob")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authService.login("bob", "anyPass"));
    }

    @Test
    void loginShouldThrowAuthenticationExceptionWhenPasswordInvalid() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(existingUser));

        assertThrows(AuthenticationException.class, () -> authService.login("alice", "wrongPass"));
    }


    @Test
    void refreshAccessTokenShouldReturnNewAccessTokenWhenRefreshTokenValid() {
        String validRefreshToken = "validRefreshToken";
        String username = "alice";
        String newAccessToken = "newAccessToken";

        when(jwtProvider.validateRefreshToken(validRefreshToken)).thenReturn(true);
        when(jwtProvider.getUsernameFromToken(validRefreshToken)).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(existingUser));
        when(jwtProvider.generateAccessToken(existingUser)).thenReturn(newAccessToken);

        AuthResponse response = authService.refreshAccessToken(validRefreshToken);

        assertNotNull(response);
        assertEquals(newAccessToken, response.accessToken());
        assertEquals(validRefreshToken, response.refreshToken());

        verify(jwtProvider).validateRefreshToken(validRefreshToken);
        verify(jwtProvider).getUsernameFromToken(validRefreshToken);
        verify(userRepository).findByUsername(username);
        verify(jwtProvider).generateAccessToken(existingUser);
    }


    @Test
    void refreshAccessTokenShouldThrowAuthenticationExceptionWhenRefreshTokenInvalid() {
        String invalidRefreshToken = "invalidRefreshToken";

        when(jwtProvider.validateRefreshToken(invalidRefreshToken)).thenReturn(false);

        assertThrows(AuthenticationException.class, () -> {
            authService.refreshAccessToken(invalidRefreshToken);
        });

        verify(jwtProvider).validateRefreshToken(invalidRefreshToken);
        verifyNoMoreInteractions(jwtProvider);
        verifyNoInteractions(userRepository);
    }
}