package io.student.service;

import io.student.dto.ProfileUpdateRequest;
import io.student.dto.UserProfileResponse;
import io.student.exception.UserNotFoundException;
import io.student.model.UserProfile;
import io.student.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserProfileRepository repository;

    @InjectMocks
    private UserProfileService service;

    @Test
    void shouldCreateEmptyProfileFromProfileRequestAfterRegister() {
        Long authId = 32L;

        UserProfile profile = new UserProfile();
        profile.setAuthUserId(authId);
        profile.setFirstName(null);
        profile.setLastName(null);
        profile.setGroupNumber(null);
        profile.setPhoneNumber(null);

        when(repository.save(any())).thenReturn(profile);

        UserProfileResponse result = service.createProfile(authId);

        assertEquals(profile.getAuthUserId(), result.authUserId());
        assertNull(result.firstName());
        assertNull(result.lastName());
        assertNull(result.groupNumber());
        assertNull(result.phoneNumber());
        verify(repository).save(any());
    }

    @Test
    void shouldReturnProfileByAuthUserId() {
        Long authUserId = 42L;

        UserProfile profile = new UserProfile();
        profile.setAuthUserId(authUserId);
        profile.setFirstName("Иван");
        profile.setLastName("Иванов");
        profile.setGroupNumber("ИС-301");
        profile.setPhoneNumber("+79990001122");

        when(repository.findByAuthUserId(authUserId))
                .thenReturn(Optional.of(profile));

        UserProfileResponse response = service.getProfileByAuthUserId(authUserId);

        assertThat(response).isNotNull();
        assertThat(response.authUserId()).isEqualTo(authUserId);
        assertThat(response.firstName()).isEqualTo("Иван");
        assertThat(response.lastName()).isEqualTo("Иванов");
        assertThat(response.groupNumber()).isEqualTo("ИС-301");
        assertThat(response.phoneNumber()).isEqualTo("+79990001122");

        verify(repository, times(1)).findByAuthUserId(authUserId);
    }

    @Test
    void shouldThrowExceptionWhenProfileNotFound() {
        Long missingAuthUserId = 99L;

        when(repository.findByAuthUserId(missingAuthUserId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            service.getProfileByAuthUserId(missingAuthUserId);
        });

        verify(repository, times(1)).findByAuthUserId(missingAuthUserId);
    }

    @Test
    void shouldUpdateAllFields() {
        UserProfile profile = new UserProfile();
        profile.setAuthUserId(1L);
        when(repository.findByAuthUserId(1L)).thenReturn(Optional.of(profile));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ProfileUpdateRequest request = new ProfileUpdateRequest("Иван", "Иванов", "ИС-301", "+79990001122");
        UserProfileResponse response = service.updateProfile(1L, request);

        assertThat(response.firstName()).isEqualTo("Иван");
        assertThat(response.lastName()).isEqualTo("Иванов");
        assertThat(response.groupNumber()).isEqualTo("ИС-301");
        assertThat(response.phoneNumber()).isEqualTo("+79990001122");
    }

    @Test
    void shouldUpdatePartialFields() {
        UserProfile profile = new UserProfile();
        profile.setAuthUserId(1L);
        profile.setFirstName("Old");
        profile.setLastName("Name");
        when(repository.findByAuthUserId(1L)).thenReturn(Optional.of(profile));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ProfileUpdateRequest request = new ProfileUpdateRequest("New", null, null, "+79990001122");
        UserProfileResponse response = service.updateProfile(1L, request);

        assertThat(response.firstName()).isEqualTo("New");
        assertThat(response.lastName()).isEqualTo("Name");
        assertThat(response.phoneNumber()).isEqualTo("+79990001122");
    }
}