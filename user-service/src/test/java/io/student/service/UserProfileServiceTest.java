package io.student.service;

import io.student.model.UserProfile;
import io.student.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserProfileRepository repository;

    @InjectMocks
    private UserProfileService service;

    @Test
    void shouldCreateProfileWithAuthUserIdOnly() {
        UserProfile profile = new UserProfile();
        profile.setAuthUserId(1L);

        when(repository.save(any())).thenReturn(profile);

        UserProfile result = service.createProfile(1L);

        assertEquals(1L, result.getAuthUserId());
        verify(repository).save(any());
    }
}