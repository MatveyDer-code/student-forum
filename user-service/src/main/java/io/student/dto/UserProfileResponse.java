package io.student.dto;

public record UserProfileResponse(
        Long id,
        Long authUserId,
        String firstName,
        String lastName,
        String groupNumber,
        String phoneNumber
) {}