package io.student.service;

import io.student.dto.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRegisteredConsumer {
    private final UserProfileService profileService;

    @KafkaListener(topics = "user-registered", groupId = "user-service-group")
    public void handleUserRegistered(UserRegisteredEvent event) {
        profileService.createProfile(event.authUserId());
    }
}