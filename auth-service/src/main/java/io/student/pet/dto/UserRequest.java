package io.student.pet.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserRequest(
        @NotBlank
        String username,

        @NotBlank
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                message = "Пароль должен содержать минимум 8 символов, одну заглавную букву, одну цифру и один специальный символ"
        )
        String password,

        @Email @NotBlank
        String email,

        @NotBlank
        String role
) {}