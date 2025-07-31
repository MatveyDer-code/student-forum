package io.student.pet.service;

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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private AuthService authService;

    User existingUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        existingUser = new User("alice", "encodedPass", "alice@example.com", new Role("STUDENT"));
        existingUser.setId(1L);
    }

    @Test
    void registerShouldCreateUserWhenRoleExists() {
        Role studentRole = new Role();
        studentRole.setName("STUDENT");

        UserRequest request = new UserRequest("alice", "pass123", "alice@example.com", studentRole.getName());

        when(roleRepository.findByName("STUDENT")).thenReturn(Optional.of(studentRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        User result = authService.register(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("alice");
        assertThat(result.getRole().getName()).isEqualTo("STUDENT");
    }

    @Test
    void registerShouldThrowExceptionWhenRoleNotExists() {
        Role studentRole = new Role();
        studentRole.setName("NO ROLE");

        UserRequest request = new UserRequest("alice", "pass123", "alice@example.com", studentRole.getName());

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

        User userById = authService.getUserById(1L);
        assertNotNull(userById);
        assertEquals(1L, userById.getId());
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
}