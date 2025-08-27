package io.student.service;

import io.student.dto.ProfileUpdateRequest;
import io.student.dto.UserProfileResponse;
import io.student.repository.UserProfileRepository;
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

    // Проверяет, что при создании профиля все дополнительные поля остаются пустыми.
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
}