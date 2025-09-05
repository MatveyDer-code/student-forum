package io.student.controller;

import io.student.dto.ProfileUpdateRequest;
import io.student.dto.UserProfileResponse;
import io.student.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService profileService;

    @GetMapping("/{authUserId}")
    public ResponseEntity<UserProfileResponse> getProfile(@PathVariable Long authUserId) {
        return ResponseEntity.ok(profileService.getProfileByAuthUserId(authUserId));
    }

    @PatchMapping("/{authUserId}")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @PathVariable Long authUserId,
            @RequestBody ProfileUpdateRequest updateRequest
    ) {
        return ResponseEntity.ok(profileService.updateProfile(authUserId, updateRequest));
    }

    @DeleteMapping("/{authUserId}")
    public ResponseEntity<Void> deleteProfile(@PathVariable Long authUserId) {
        profileService.deleteProfile(authUserId);
        return ResponseEntity.noContent().build();
    }
}