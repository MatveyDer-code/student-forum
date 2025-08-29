package io.student.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDeletedProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendUserDeletedEvent(Long authUserId) {
        kafkaTemplate.send("user-deleted", authUserId.toString());
    }
}
