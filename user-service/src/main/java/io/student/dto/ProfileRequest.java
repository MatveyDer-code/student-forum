package io.student.dto;

public record ProfileRequest(
        Long authUserId,
        String firstName,
        String lastName,
        String groupNumber,
        String phoneNumber
) {}
