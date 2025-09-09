package io.student.pet.service;

import io.student.pet.dto.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRegisteredProducer {
    private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;

    public void sendUserRegisteredEvent(Long authUserId) {
        kafkaTemplate.send("user-registered", new UserRegisteredEvent(authUserId));
    }
}