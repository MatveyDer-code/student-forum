package io.student.controller;

import io.student.dto.ProfileUpdateRequest;
import io.student.dto.UserProfileResponse;
import io.student.exception.UserNotFoundException;
import io.student.service.UserProfileService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserProfileController.class)
class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserProfileService profileService;

    @Test
    void shouldReturnProfile() throws Exception {
        Long authUserId = 42L;
        UserProfileResponse response = new UserProfileResponse(authUserId, authUserId, "Ivan", "Ivanov", "IS-301", "+79990001122");

        when(profileService.getProfileByAuthUserId(authUserId)).thenReturn(response);

        mockMvc.perform(get("/profile/{authUserId}", authUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authUserId").value(authUserId))
                .andExpect(jsonPath("$.firstName").value("Ivan"))
                .andExpect(jsonPath("$.lastName").value("Ivanov"));
    }

    @Test
    void shouldUpdateProfile() throws Exception {
        Long authUserId = 42L;
        UserProfileResponse updatedResponse = new UserProfileResponse(authUserId, authUserId, "Petr", "Ivanov", "IS-301", "+79990001122");

        when(profileService.updateProfile(eq(authUserId), any(ProfileUpdateRequest.class))).thenReturn(updatedResponse);

        mockMvc.perform(patch("/profile/{authUserId}", authUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Petr\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Petr"));
    }

    @Test
    void shouldDeleteProfile() throws Exception {
        Long authUserId = 42L;

        Mockito.doNothing().when(profileService).deleteProfile(authUserId);

        mockMvc.perform(delete("/profile/{authUserId}", authUserId))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnNotFoundForMissingProfile() throws Exception {
        Long authUserId = 99L;
        when(profileService.getProfileByAuthUserId(authUserId))
                .thenThrow(new UserNotFoundException("Profile not found"));

        mockMvc.perform(get("/profile/{authUserId}", authUserId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Profile not found"));
    }
}