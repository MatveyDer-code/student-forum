package io.student.pet.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenUsernameIsBlankThenValidationFails() {
        UserRequest request = new UserRequest("", "password", "user@example.com", "STUDENT");

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("username"))).isTrue();
    }

    @Test
    void whenEmailIsInvalidThenValidationFails() {
        UserRequest request = new UserRequest("user", "password", "invalid-email", "STUDENT");

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email"))).isTrue();
    }

    @Test
    void validPasswordShouldPassValidation() {
        UserRequest user = new UserRequest(
                "username",
                "StrongP@ss1",
                "user@example.com",
                "STUDENT"
        );

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Пароль должен пройти валидацию");
    }

    @Test
    void invalidPasswordShouldFailValidation() {
        UserRequest user = new UserRequest(
                "username",
                "weakpass",
                "user@example.com",
                "STUDENT"
        );

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Пароль должен не пройти валидацию");

        boolean hasPasswordError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password"));

        assertTrue(hasPasswordError, "Ошибка должна быть связана с полем password");
    }

    @Test
    void whenAllFieldsValidThenNoViolations() {
        UserRequest request = new UserRequest("user", "StrongP@ss1", "user@example.com", "STUDENT");

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }
}