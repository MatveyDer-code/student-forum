package io.student.service;

import io.student.dto.ProfileUpdateRequest;
import io.student.dto.UserProfileResponse;
import io.student.exception.UserNotFoundException;
import io.student.repository.UserProfileRepository;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import java.time.Duration;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
public class UserProfileServiceIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    //Не робит контейнер не может поднять
//    @Container
//    static ConfluentKafkaContainer kafka = new ConfluentKafkaContainer(
//            DockerImageName.parse("confluentinc/cp-kafka:7.5.1"))
//            .withExposedPorts(9092)
//            .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true")
//            .withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1")
//            .withEnv("KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR", "1")
//            .withEnv("KAFKA_TRANSACTION_STATE_LOG_MIN_ISR", "1")
//            .withEnv("KAFKA_BROKER_ID", "1")
//            .withEnv("KAFKA_LISTENERS", "PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093")
//            .withEnv("KAFKA_CONTROLLER_LISTENER_NAMES", "CONTROLLER")
//            .withEnv("KAFKA_PROCESS_ROLES", "broker,controller")
//            .withEnv("KAFKA_ADVERTISED_LISTENERS", "PLAINTEXT://localhost:9092");

    @Autowired
    private UserDeletedProducer producer;

    @Autowired
    private UserProfileService profileService;

    @Autowired
    private UserProfileRepository profileRepository;

    @Autowired
    private KafkaAdmin kafkaAdmin;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        //registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
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

//    @Test
//    void shouldDeleteProfileAndPublishKafkaEvent() {
//        String topic = "user-deleted";
//        Long authUserId = 42L;
//
//        // Ensure topic exists before producing message
//        //ensureTopicExists(topic);
//
//        UserProfileResponse created = profileService.createProfile(authUserId);
//        assertThat(created).isNotNull();
//
//        profileService.deleteProfile(authUserId);
//
//        Map<String, Object> consumerProps = new HashMap<>(KafkaTestUtils.consumerProps("testGroup", "true", kafka.getBootstrapServers()));
//        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
//        DefaultKafkaConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(
//                consumerProps,
//                new StringDeserializer(),
//                new StringDeserializer()
//        );
//        try (Consumer<String, String> consumer = cf.createConsumer()) {
//            consumer.subscribe(Collections.singleton(topic));
//            ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(10));
//            assertThat(records.count()).isGreaterThan(0);
//            String value = records.iterator().next().value();
//            assertThat(value).contains("42");
//        }
//
//        assertThatThrownBy(() -> profileService.getProfileByAuthUserId(authUserId))
//                .isInstanceOf(UserNotFoundException.class)
//                .hasMessageContaining("authUserId=" + authUserId);
//    }
}