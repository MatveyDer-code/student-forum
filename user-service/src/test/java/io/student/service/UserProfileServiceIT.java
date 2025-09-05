package io.student.service;

import io.student.dto.ProfileUpdateRequest;
import io.student.dto.UserProfileResponse;
import io.student.exception.UserNotFoundException;
import io.student.repository.UserProfileRepository;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.*;

import java.time.Duration;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
class UserProfileServiceIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static final KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("apache/kafka-native:4.0.0")
    );

    @Autowired
    private UserDeletedProducer producer;

    @Autowired
    private UserProfileService profileService;

    @Autowired
    private UserProfileRepository profileRepository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Test
    void shouldCreateEmptyProfileAndRetrieve() {
        Long authUserId = 42L;

        UserProfileResponse created = profileService.createProfile(authUserId);
        assertThat(created).isNotNull();
        assertThat(created.authUserId()).isEqualTo(authUserId);

        UserProfileResponse fetched = profileService.getProfileByAuthUserId(authUserId);
        assertThat(fetched).isNotNull();
        assertThat(fetched.authUserId()).isEqualTo(authUserId);
        assertThat(fetched.firstName()).isNull();
        assertThat(fetched.lastName()).isNull();
        assertThat(fetched.groupNumber()).isNull();
        assertThat(fetched.phoneNumber()).isNull();
    }

    @Test
    void shouldCreateUpdateAndGetProfile() {
        long authUserId = System.currentTimeMillis() % Integer.MAX_VALUE;

        UserProfileResponse created = profileService.createProfile(authUserId);

        ProfileUpdateRequest update = new ProfileUpdateRequest(
                "Иван", "Иванов", "ИС-301", "+79990001122"
        );
        UserProfileResponse updated = profileService.updateProfile(authUserId, update);

        assertThat(updated.authUserId()).isEqualTo(authUserId);
        assertThat(updated.firstName()).isEqualTo("Иван");
        assertThat(updated.lastName()).isEqualTo("Иванов");
        assertThat(updated.groupNumber()).isEqualTo("ИС-301");
        assertThat(updated.phoneNumber()).isEqualTo("+79990001122");

        UserProfileResponse fetched = profileService.getProfileByAuthUserId(authUserId);
        assertThat(fetched.firstName()).isEqualTo("Иван");
        assertThat(fetched.lastName()).isEqualTo("Иванов");
        assertThat(fetched.groupNumber()).isEqualTo("ИС-301");
        assertThat(fetched.phoneNumber()).isEqualTo("+79990001122");
    }

    @Test
    void shouldDeleteProfileAndPublishKafkaEvent() throws ExecutionException, InterruptedException {
        String topic = "user-deleted";
        Long authUserId = 42L;

        profileService.createProfile(authUserId);

        profileService.deleteProfile(authUserId);

        assertThatThrownBy(() -> profileService.getProfileByAuthUserId(authUserId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("authUserId=" + authUserId);

        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {
            consumer.subscribe(Collections.singleton(topic));

            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));
            assertThat(records.count()).isGreaterThan(0);

            String value = records.iterator().next().value();
            assertThat(value).contains(authUserId.toString());
        }
    }
}