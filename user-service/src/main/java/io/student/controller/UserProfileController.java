package io.student.controller;

import io.student.dto.ProfileUpdateRequest;
import io.student.dto.UserProfileResponse;
import io.student.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService profileService;

    @GetMapping("/{authUserId}")
    public UserProfileResponse getProfile(@PathVariable Long authUserId) {
        return profileService.getProfileByAuthUserId(authUserId);
    }

    @PatchMapping("/{authUserId}")
    public UserProfileResponse updateProfile(
            @PathVariable Long authUserId,
            @RequestBody ProfileUpdateRequest updateRequest
    ) {
        return profileService.updateProfile(authUserId, updateRequest);
    }

    @DeleteMapping("/{authUserId}")
    public void deleteProfile(@PathVariable Long authUserId) {
        profileService.deleteProfile(authUserId);
    }
}