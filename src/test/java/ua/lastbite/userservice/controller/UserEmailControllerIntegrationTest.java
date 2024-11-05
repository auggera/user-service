package ua.lastbite.userservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import ua.lastbite.userservice.model.CountryCode;
import ua.lastbite.userservice.model.User;
import ua.lastbite.userservice.model.UserRole;
import ua.lastbite.userservice.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;
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
    JdbcTemplate jdbcTemplate;

    private static final Integer USER_ID = 1;
    private User user;

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
    }

    @Test
    void testGetUserEmailInfoSuccessfully() throws Exception {
        userRepository.save(user);

        mockMvc.perform(get("/api/email/{userId}/info", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.verified").value(false));
    }

    @Test
    void testGetUserEmailInfoUserNotFound() throws Exception {
        mockMvc.perform(get("/api/email/{userId}/info", USER_ID))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User with ID 1 not found"));
    }

    @Test
    void verifyEmail_Success() throws Exception {
        userRepository.save(user);

        mockMvc.perform(post("/api/email/{userId}/verify-email", USER_ID))
                .andExpect(status().isOk())
                .andExpect(content().string("Email successfully verified"));

        User updatedUser = userRepository.findById(USER_ID).orElseThrow();
        assertTrue(updatedUser.isEmailVerified());
    }

    @Test
    void verifyEmail_UserNotFound() throws Exception {

        mockMvc.perform(post("/api/email/{userId}/verify-email", USER_ID))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User with ID 1 not found"));
    }
}
