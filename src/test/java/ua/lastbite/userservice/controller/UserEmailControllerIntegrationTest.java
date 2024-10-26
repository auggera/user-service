package ua.lastbite.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import ua.lastbite.userservice.dto.token.TokenValidationRequest;
import ua.lastbite.userservice.dto.token.TokenValidationResponse;
import ua.lastbite.userservice.model.CountryCode;
import ua.lastbite.userservice.model.User;
import ua.lastbite.userservice.model.UserRole;
import ua.lastbite.userservice.repository.UserRepository;
import ua.lastbite.userservice.service.TokenServiceClient;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class UserEmailControllerIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    TokenServiceClient tokenServiceClient;

    @Autowired
    JdbcTemplate jdbcTemplate;

    private User user;
    private TokenValidationRequest tokenValidationRequest;
    private TokenValidationResponse tokenValidationResponse;

    @BeforeEach
    void cleanUpDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE app_user RESTART IDENTITY");
    }

    @BeforeEach
    void setUp() {
        user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@example.com");
        user.setPassword("password123");
        user.setPhoneNumber("123456789");
        user.setCountryCode(CountryCode.UA);
        user.setRole(UserRole.CUSTOMER);

        tokenValidationRequest = new TokenValidationRequest("tokenValue123");
        tokenValidationResponse = new TokenValidationResponse(true, 1);

    }

    @Test
    void testGetUserEmailInfoSuccessfully() throws Exception {
        userRepository.save(user);

        mockMvc.perform(get("/api/email/1/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.verified").value(false));
    }

    @Test
    void testGetUserEmailInfoUserNotFound() throws Exception {
        mockMvc.perform(get("/api/email/1/info"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User with ID 1 not found"));
    }

    @Test
    void testVerifyEmailSuccessfully() throws Exception {
        userRepository.save(user);

        Mockito.when(tokenServiceClient.verifyToken(tokenValidationRequest))
                .thenReturn(tokenValidationResponse);

        mockMvc.perform(post("/api/email/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tokenValidationRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testVerifyEmailUserNotFound() throws Exception {

        Mockito.when(tokenServiceClient.verifyToken(tokenValidationRequest))
                .thenReturn(tokenValidationResponse);

        mockMvc.perform(post("/api/email/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenValidationRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User with ID 1 not found"));
    }

    @Test
    void testVerifyEmailWithNullToken() throws Exception {
        tokenValidationRequest.setTokenValue(null);

        Mockito.when(tokenServiceClient.verifyToken(tokenValidationRequest))
                .thenReturn(tokenValidationResponse);

        mockMvc.perform(post("/api/email/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenValidationRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.tokenValue").value("Token cannot be empty"));
    }

    @Test
    void testVerifyEmailRequestIsNull() throws Exception {
        tokenValidationRequest = null;

        mockMvc.perform(post("/api/email/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenValidationRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Request body is missing or invalid"));
    }
}
