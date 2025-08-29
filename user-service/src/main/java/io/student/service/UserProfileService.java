package io.student.service;

import io.student.dto.ProfileUpdateRequest;
import io.student.dto.UserProfileResponse;
import io.student.exception.UserNotFoundException;
import io.student.model.UserProfile;
import io.student.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository repository;
    private final UserDeletedProducer userDeletedProducer;

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


    public UserProfileResponse updateProfile(long authUserId, ProfileUpdateRequest updateRequest) {
        UserProfile profile = repository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new UserNotFoundException("Профиль с authUserId=" + authUserId + " не найден"));

        if (updateRequest.firstName() != null) profile.setFirstName(updateRequest.firstName());
        if (updateRequest.lastName() != null) profile.setLastName(updateRequest.lastName());
        if (updateRequest.groupNumber() != null) profile.setGroupNumber(updateRequest.groupNumber());
        if (updateRequest.phoneNumber() != null) profile.setPhoneNumber(updateRequest.phoneNumber());

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

    public void deleteProfile(long authUserId) {
        UserProfile profile = repository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new UserNotFoundException("Профиль с authUserId=" + authUserId + " не найден"));

        repository.delete(profile);
        userDeletedProducer.sendUserDeletedEvent(authUserId);
    }
}