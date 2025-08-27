package io.student.dto;

public record ProfileUpdateRequest(
    String firstName,
    String lastName,
    String groupNumber,
    String phoneNumber
) {}
