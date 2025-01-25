package ua.lastbite.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ua.lastbite.userservice.dto.user.UserRegistrationRequestDto;
import ua.lastbite.userservice.model.CountryCode;
import ua.lastbite.userservice.model.User;
import ua.lastbite.userservice.model.UserRole;
import ua.lastbite.userservice.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerRegistrationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    JdbcTemplate jdbcTemplate;

    private static final long USER_ID = 1;
    UserRegistrationRequestDto userRegistrationRequestDto;
    User existingUser;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE users RESTART IDENTITY");
    }

    @BeforeEach
    void setUpUserRegistrationRequest() {
        userRegistrationRequestDto = new UserRegistrationRequestDto();
        userRegistrationRequestDto.setFirstName("John");
        userRegistrationRequestDto.setLastName("Doe");
        userRegistrationRequestDto.setEmail("john@example.com");
        userRegistrationRequestDto.setPassword("password123");
        userRegistrationRequestDto.setCountryCode(CountryCode.UA);
        userRegistrationRequestDto.setPhoneNumber("123456789");
        userRegistrationRequestDto.setRole(UserRole.CUSTOMER);

        existingUser = new User();
        existingUser.setId(USER_ID);
        existingUser.setFirstName("Jane");
        existingUser.setLastName("Doe");
        existingUser.setEmail("jane@example.com");
        existingUser.setPassword(passwordEncoder.encode("password123"));
        existingUser.setCountryCode(CountryCode.UA);
        existingUser.setPhoneNumber("987654321");
        existingUser.setRole(UserRole.CUSTOMER);
    }

    @Test
    void testRegisterUserSuccessfully() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegistrationRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        Optional<User> savedUser = userRepository.findByEmail("john@example.com");
        assertTrue(savedUser.isPresent());
        assertEquals("John", savedUser.get().getFirstName());
        assertEquals("Doe", savedUser.get().getLastName());
    }

    @Test
    void testRegisterUserWithInvalidEmail() throws Exception {
        userRegistrationRequestDto.setEmail("invalidEmail");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegistrationRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Invalid email format"));
    }

    @Test
    void testRegisterUserWithExistingEmail() throws Exception {
        existingUser.setEmail("john@example.com");
        userRepository.save(existingUser);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegistrationRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email " + existingUser.getEmail() + " is already in use"));
    }

    @Test
    void testRegisterUserWithEmptyEmail() throws Exception {
        userRegistrationRequestDto.setEmail(null);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegistrationRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Email cannot be empty"));
    }

    @Test
    void testRegisterUserWithExistingPhoneNumber() throws Exception {
        existingUser.setPhoneNumber("123456789");
        userRepository.save(existingUser);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegistrationRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Phone number " + userRegistrationRequestDto.getPhoneNumber() + " is already in use"));
    }

    @Test
    void testRegisterUserPhoneNumberTooLong() throws Exception {
        userRegistrationRequestDto.setPhoneNumber("123456789000");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegistrationRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.phoneNumber").value("Invalid phone number format"));
    }

    @Test
    void testRegisterUserPhoneNumberTooShort() throws Exception {
        userRegistrationRequestDto.setPhoneNumber("12345");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegistrationRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.phoneNumber").value("Invalid phone number format"));
    }

    @Test
    void testRegisterUserWithInvalidPhoneNumberFormat() throws Exception {
        userRegistrationRequestDto.setPhoneNumber("312#-invalid");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegistrationRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.phoneNumber").value("Invalid phone number format"));
    }

    @Test
    void testRegisterUserWithEmptyPhoneNumber() throws Exception {
        userRegistrationRequestDto.setPhoneNumber(null);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegistrationRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.phoneNumber").value("Phone number cannot be empty"));
    }

    @Test
    void testRegisterUserWithShortName() throws Exception {
        userRegistrationRequestDto.setFirstName("B");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegistrationRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.firstName").value("First name must be between 2 and 100 characters"));
    }

    @Test
    void testRegisterUserWithEmptyName() throws Exception {
        userRegistrationRequestDto.setFirstName(null);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegistrationRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.firstName").value("First name cannot be empty"));
    }

    @Test
    void testRegisterUserWithInvalidLastNameFormat() throws Exception {
        userRegistrationRequestDto.setLastName("Doe13");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegistrationRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.lastName").value("Invalid name format"));
    }

    @Test
    void testRegisterUserWithEmptyPassword() throws Exception {
        userRegistrationRequestDto.setPassword(null);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegistrationRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").value("Password cannot be empty"));
    }

    @Test
    void testRegisterUserWithShortPassword() throws Exception {
        userRegistrationRequestDto.setPassword("short1");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegistrationRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").value("Password must be at least 8 characters long and contain at least one letter and one number"));
    }

    @Test
    void testRegisterUserWithEmptyCountryCode() throws Exception {
        userRegistrationRequestDto.setCountryCode(null);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegistrationRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.countryCode").value("Country code is required"));
    }

    @Test
    void testRegisterUserWithNoNumbersPassword() throws Exception {
        userRegistrationRequestDto.setPassword("password");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegistrationRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").value("Password must be at least 8 characters long and contain at least one letter and one number"));
    }

    @Test
    void testRegisterUserWithEmptyRole() throws Exception {
        userRegistrationRequestDto.setRole(null);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegistrationRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.role").value("Role is required"));
    }
}