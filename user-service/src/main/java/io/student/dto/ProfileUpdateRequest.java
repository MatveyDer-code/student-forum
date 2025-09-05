package io.student.dto;

import jakarta.validation.constraints.Pattern;

public record ProfileUpdateRequest(
        @Pattern(regexp = "^[A-Za-zА-Яа-яЁё]+$", message = "Имя может содержать только буквы")
        String firstName,

        @Pattern(regexp = "^[A-Za-zА-Яа-яЁё]+$", message = "Фамилия может содержать только буквы")
        String lastName,

        @Pattern(regexp = "^[A-Za-z0-9-]+$", message = "Номер группы может содержать только буквы, цифры и дефис")
        String groupNumber,

        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Некорректный номер телефона")
        String phoneNumber
) {}