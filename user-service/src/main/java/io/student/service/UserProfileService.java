package io.student.service;

import io.student.model.UserProfile;
import io.student.repository.UserProfileRepository;
import org.springframework.stereotype.Service;

@Service
public class UserProfileService {

    private final UserProfileRepository repository;

    public UserProfileService(UserProfileRepository repository) {
        this.repository = repository;
    }

    public UserProfile createProfile(Long authUserId) {
        UserProfile profile = new UserProfile();
        profile.setAuthUserId(authUserId);
        return repository.save(profile);
    }
}