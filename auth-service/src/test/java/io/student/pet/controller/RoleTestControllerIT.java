package io.student.pet.controller;

import io.student.pet.model.User;
import io.student.pet.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonNode;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.util.JSONPObject;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "jwt.secret=verySecretKeyForJwtGeneration1234567890",
        "jwt.access-expiration=3600000",
        "jwt.refresh-expiration=604800000"
})
class RoleTestControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void propertySource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;

    @Test
    void moderatorEndpointShouldReturn200ForModeratorUser() throws Exception {
        String loginJson = """
        {
            "username": "testmoderator",
            "password": "password2"
        }
        """;

        String loginResponse = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(loginResponse);

        String token = json.get("accessToken").asText();

        mockMvc.perform(get("/test/moderator")
                    .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void teacherEndpointShouldReturn200ForTeacherUser() throws Exception {
        String loginJson = """
    {
        "username": "testteacher",
        "password": "password3"
    }
    """;

        String loginResponse = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(loginResponse);

        String token = json.get("accessToken").asText();

        mockMvc.perform(get("/test/teacher")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void studentEndpointShouldReturn200ForStudentUser() throws Exception {
        String loginJson = """
    {
        "username": "teststudent",
        "password": "password1"
    }
    """;

        String loginResponse = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(loginResponse);

        String token = json.get("accessToken").asText();

        mockMvc.perform(get("/test/student")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void moderatorEndpointShouldReturnForbiddenForStudentUser() throws Exception {
        String loginJson = """
    {
        "username": "teststudent",
        "password": "password1"
    }
    """;

        String loginResponse = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(loginResponse);

        String token = json.get("accessToken").asText();

        mockMvc.perform(get("/test/moderator")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void teacherEndpointShouldReturnForbiddenForModeratorUser() throws Exception {
        String loginJson = """
    {
        "username": "testmoderator",
        "password": "password2"
    }
    """;

        String loginResponse = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(loginResponse);

        String token = json.get("accessToken").asText();

        mockMvc.perform(get("/test/teacher")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void studentEndpointShouldReturnForbiddenForTeacherUser() throws Exception {
        String loginJson = """
    {
        "username": "testteacher",
        "password": "password3"
    }
    """;

        String loginResponse = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(loginResponse);

        String token = json.get("accessToken").asText();

        mockMvc.perform(get("/test/student")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}