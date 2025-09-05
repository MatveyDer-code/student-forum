package io.student.pet.service;

import io.student.pet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDeletedConsumer {

    private final UserRepository userRepository;

    @KafkaListener(topics = "user-deleted", groupId = "auth-service-group")
    public void consumeUserDeleted(String authUserIdStr) {
        Long authUserId = Long.parseLong(authUserIdStr);
        userRepository.findById(authUserId).ifPresent(userRepository::delete);
    }
}