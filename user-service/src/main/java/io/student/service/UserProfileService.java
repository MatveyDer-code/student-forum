package io.student.service;

import io.student.dto.UserProfileResponse;
import io.student.exception.UserNotFoundException;
import io.student.model.UserProfile;
import io.student.repository.UserProfileRepository;
import org.springframework.stereotype.Service;

@Service
public class UserProfileService {

    private final UserProfileRepository repository;

    public UserProfileService(UserProfileRepository repository) {
        this.repository = repository;
    }

    public UserProfileResponse createProfile(Long authUserId) {
        UserProfile profile = new UserProfile();
        profile.setAuthUserId(authUserId);
        profile.setFirstName(null);
        profile.setLastName(null);
        profile.setGroupNumber(null);
        profile.setPhoneNumber(null);

        UserProfile saved = repository.save(profile);
        return new UserProfileResponse(
                saved.getId(),
                saved.getAuthUserId(),
                saved.getFirstName(),
                saved.getLastName(),
                saved.getGroupNumber(),
                saved.getPhoneNumber()
        );
    }

    public UserProfileResponse getProfileByAuthUserId(Long authUserId) {
        UserProfile profile = repository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new UserNotFoundException("Профиль с authUserId=" + authUserId + " не найден"));

        return new UserProfileResponse(
                profile.getId(),
                profile.getAuthUserId(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getGroupNumber(),
                profile.getPhoneNumber()
        );
    }
}