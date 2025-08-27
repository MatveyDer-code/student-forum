package io.student.service;

import io.student.dto.ProfileRequest;
import io.student.dto.UserProfileResponse;
import io.student.model.UserProfile;
import io.student.repository.UserProfileRepository;
import jakarta.annotation.sql.DataSourceDefinition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
public class UserProfileServiceIT {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserProfileService profileService;

    @Autowired
    private UserProfileRepository profileRepository;

    @Test
    void shouldCreateAndRetrieveProfile() {
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
}