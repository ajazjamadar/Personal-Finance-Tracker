package com.qburst.training.personalfinancetracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qburst.training.personalfinancetracker.dto.UserDto;
import com.qburst.training.personalfinancetracker.entity.User;
import com.qburst.training.personalfinancetracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;

    private User savedUser;

    @BeforeEach
    void setUp(){
        savedUser = userRepository.save(User.builder()
                .username("ejaz")
                .email("ejaz@email.com")
                .passwordHash("hashed")
                .fullName("Ejaz Jamadar")
                .build());
    }

    @Test
    @DisplayName("POST/api/users: should create a user and return 201")
    void createUser_shouldReturn201() throws Exception{
        UserDto.Request request = new UserDto.Request(
                "newuser", "new@qburst.com", "password123", "New User"
        );

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("new@qburst.com"))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/users : should return 409 when email already exists")
    void createUser_shouldFail_whenEmailDuplicate() throws Exception {
        UserDto.Request request = new UserDto.Request(
                                "another", "ejaz@email.com", "password123", "Another User");

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

        @Test
        @DisplayName("POST/api/users: should return 400 when the request is invalid")
        void createUser_shouldReturn400_whenInvalidRequest() throws Exception{
        String invalidJson = """
                {
                                "username":"ab",
                "email":"not-email",
                "password":"123",
                                "fullName":""
                }
                """;

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/users/{id}: should return the user when found")
        void getUserById_shouldReturn200_whenFound() throws Exception{
        mockMvc.perform(get("/api/users/{id}", savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("ejaz"))
                .andExpect(jsonPath("$.email").value("ejaz@email.com"))
                                .andExpect(jsonPath("$.fullName").value("Ejaz Jamadar"));
    }

    @Test
    @DisplayName("GET/api/users/{id}: should return 404 when not found")
    void getUserById_shouldReturn404_whenNotFound() throws Exception{
        mockMvc.perform(get("/api/users/{id}", 99999L))
                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.error").value("User not found with id: 99999"));
    }
}