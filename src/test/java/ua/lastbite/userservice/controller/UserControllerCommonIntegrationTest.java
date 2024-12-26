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
import ua.lastbite.userservice.dto.user.ChangeEmailRequestDto;
import ua.lastbite.userservice.dto.user.UpdateNameRequestDto;
import ua.lastbite.userservice.dto.user.ChangePasswordRequestDto;
import ua.lastbite.userservice.dto.user.ChangePhoneNumberRequestDto;
import ua.lastbite.userservice.model.CountryCode;
import ua.lastbite.userservice.model.User;
import ua.lastbite.userservice.model.UserRole;
import ua.lastbite.userservice.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerCommonIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void cleanDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE app_user RESTART IDENTITY");
    }

    User existingUser;
    ChangeEmailRequestDto changeEmailRequestDto;
    ChangePasswordRequestDto changePasswordRequestDto;
    ChangePhoneNumberRequestDto changePhoneNumberRequestDto;
    UpdateNameRequestDto updateNameRequestDto;

    @BeforeEach
    void setUpUser() {
        existingUser = new User();
        existingUser.setFirstName("Jane");
        existingUser.setLastName("Doe");
        existingUser.setEmail("jane@example.com");
        existingUser.setPassword(passwordEncoder.encode("password123"));
        existingUser.setCountryCode(CountryCode.UA);
        existingUser.setPhoneNumber("987654321");
        existingUser.setRole(UserRole.CUSTOMER);
    }

    @Test
    void testGetAllUsers() throws Exception {
        User anotherUser = new User();
        anotherUser.setFirstName("John");
        anotherUser.setLastName("Doe");
        anotherUser.setEmail("john@example.com");
        anotherUser.setPassword("password123");
        anotherUser.setCountryCode(CountryCode.UA);
        anotherUser.setPhoneNumber("123456789");
        anotherUser.setRole(UserRole.CUSTOMER);

        userRepository.save(existingUser);
        userRepository.save(anotherUser);

        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].firstName").value("Jane"))
                .andExpect(jsonPath("$[0].lastName").value("Doe"))
                .andExpect(jsonPath("$[0].email").value("jane@example.com"))
                .andExpect(jsonPath("$[1].firstName").value("John"))
                .andExpect(jsonPath("$[1].lastName").value("Doe"))
                .andExpect(jsonPath("$[1].email").value("john@example.com"));
    }

    @Test
    void testGetUserById() throws Exception {
        userRepository.save(existingUser);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("jane@example.com"))
                .andExpect(jsonPath("$.role").value(UserRole.CUSTOMER.name()));
    }

    @Test
    void testGetUserByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User with ID 1 not found"));
    }

    @BeforeEach
    void setUpChangeEmailRequest() {
        changeEmailRequestDto = new ChangeEmailRequestDto("new@example.com");
    }

    @Test
    void testChangeEmail() throws Exception {
        userRepository.save(existingUser);

        mockMvc.perform(put("/api/users/1/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeEmailRequestDto)))
                .andExpect(status().isNoContent());

        User updatedUser = userRepository.findById(1).orElse(null);
        assertNotNull(updatedUser);
        assertEquals(changeEmailRequestDto.getNewEmail(), updatedUser.getEmail());
    }

    @Test
    void testChangeNewEmailIsNull() throws Exception {
        changeEmailRequestDto.setNewEmail(null);

        mockMvc.perform(put("/api/users/1/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeEmailRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.newEmail").value("New email cannot be empty"));
    }

    @Test
    void testChangeEmailInvalidEmail() throws Exception {
        changeEmailRequestDto.setNewEmail("invalid@email..com");

        mockMvc.perform(put("/api/users/1/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeEmailRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.newEmail").value("Invalid email format"));
    }

    @Test
    void testChangeEmailNotChanged() throws Exception {
        changeEmailRequestDto.setNewEmail("jane@example.com");
        userRepository.save(existingUser);

        mockMvc.perform(put("/api/users/1/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeEmailRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("New email is the same as the current email"));
    }

    @BeforeEach
    void setUpChangePasswordRequest() {
        changePasswordRequestDto = new ChangePasswordRequestDto();
        changePasswordRequestDto.setCurrentPassword("password123");
        changePasswordRequestDto.setNewPassword("newPassword123");
    }

    @Test
    void testChangePassword() throws Exception {
        userRepository.save(existingUser);

        mockMvc.perform(put("/api/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequestDto)))
                .andExpect(status().isNoContent());

        User updatedUser = userRepository.findById(1).orElse(null);
        assertNotNull(updatedUser);
        assertTrue(passwordEncoder.matches(changePasswordRequestDto.getNewPassword(), updatedUser.getPassword()));
    }

    @Test
    void testChangePasswordNewPasswordIsNull() throws Exception {
        changePasswordRequestDto.setNewPassword(null);
        userRepository.save(existingUser);

        mockMvc.perform(put("/api/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.newPassword").value("New password cannot be empty"));
    }

    @Test
    void testChangePasswordCurrentPasswordIsNull() throws Exception {
        changePasswordRequestDto.setCurrentPassword(null);
        userRepository.save(existingUser);

        mockMvc.perform(put("/api/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.currentPassword").value("Current password cannot be empty"));
    }

    @Test
    void testChangePasswordIncorrectCurrentPassword() throws Exception {
        changePasswordRequestDto.setCurrentPassword("invalid");
        userRepository.save(existingUser);

        mockMvc.perform(put("/api/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Current password is incorrect"));
    }

    @Test
    void testChangePasswordInvalidNewPassword() throws Exception {
        changePasswordRequestDto.setNewPassword("invalid");
        userRepository.save(existingUser);

        mockMvc.perform(put("/api/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.newPassword").value("Password must be at least 8 characters long and contain at least one letter and one number"));
    }

    @Test
    void testChangePasswordNotChanged() throws Exception {
        changePasswordRequestDto.setNewPassword("password123");
        userRepository.save(existingUser);

        mockMvc.perform(put("/api/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("New password cannot be the same as the current password"));
    }

    @BeforeEach
    void setUpChangePhoneNumberRequest() {
        changePhoneNumberRequestDto = new ChangePhoneNumberRequestDto();
        changePhoneNumberRequestDto.setCountryCode(CountryCode.UA);
        changePhoneNumberRequestDto.setNewPhoneNumber("123456789");
    }

    @Test
    void testChangePhoneNumber() throws Exception {
        userRepository.save(existingUser);

        mockMvc.perform(put("/api/users/1/phone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePhoneNumberRequestDto)))
                .andExpect(status().isNoContent());

        User updatedUser = userRepository.findById(1).orElse(null);
        assertNotNull(updatedUser);
        assertEquals(changePhoneNumberRequestDto.getNewPhoneNumber(), updatedUser.getPhoneNumber());
    }

    @Test
    void testChangePhoneNumberInvalidPhoneNumber() throws Exception {
        changePhoneNumberRequestDto.setNewPhoneNumber("abc123456");
        userRepository.save(existingUser);

        mockMvc.perform(put("/api/users/1/phone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePhoneNumberRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.newPhoneNumber").value("Invalid phone number format"));
    }

    @Test
    void testChangePhoneNumberIsNull() throws Exception {
        changePhoneNumberRequestDto.setNewPhoneNumber(null);
        userRepository.save(existingUser);

        mockMvc.perform(put("/api/users/1/phone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePhoneNumberRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.newPhoneNumber").value("Phone number cannot be empty"));
    }

    @Test
    void testChangePhoneNumberCountryCodeIsNull() throws Exception {
        changePhoneNumberRequestDto.setCountryCode(null);
        userRepository.save(existingUser);

        mockMvc.perform(put("/api/users/1/phone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePhoneNumberRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.countryCode").value("Country code is required"));
    }

    @Test
    void testChangePhoneNumberTooLong() throws Exception {
        changePhoneNumberRequestDto.setNewPhoneNumber("12345678901234");
        userRepository.save(existingUser);

        mockMvc.perform(put("/api/users/1/phone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePhoneNumberRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.newPhoneNumber").value("Invalid phone number format"));
    }

    @Test
    void testChangePhoneNumberTooShort() throws Exception {
        changePhoneNumberRequestDto.setNewPhoneNumber("12345");
        userRepository.save(existingUser);

        mockMvc.perform(put("/api/users/1/phone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePhoneNumberRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.newPhoneNumber").value("Invalid phone number format"));
    }

    @Test
    void testChangePhoneNumberNotChanged() throws Exception {
        changePhoneNumberRequestDto.setNewPhoneNumber("987654321");
        userRepository.save(existingUser);

        mockMvc.perform(put("/api/users/1/phone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePhoneNumberRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("New phone number cannot be the same as the current phone number"));
    }

    @BeforeEach
    void setUpChangeNameRequest() {
        updateNameRequestDto = new UpdateNameRequestDto();
        updateNameRequestDto.setFirstName("John");
        updateNameRequestDto.setLastName("Jackson");
    }

    @Test
    void testChangeName() throws Exception {
        userRepository.save(existingUser);

        mockMvc.perform(put("/api/users/1/name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateNameRequestDto)))
                .andExpect(status().isNoContent());

        User updatedUser = userRepository.findById(1).orElse(null);
        assertNotNull(updatedUser);
        assertEquals(updateNameRequestDto.getFirstName(), updatedUser.getFirstName());
        assertEquals(updateNameRequestDto.getLastName(), updatedUser.getLastName());
    }

    @Test
    void testChangeNameShortFirstName() throws Exception {
        updateNameRequestDto.setFirstName("J");
        userRepository.save(existingUser);

        mockMvc.perform(put("/api/users/1/name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateNameRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.firstName").value("First name must be between 2 and 100 characters"));
    }

    @Test
    void testChangeNameLastNameIsNull() throws Exception {
        updateNameRequestDto.setLastName(null);
        userRepository.save(existingUser);

        mockMvc.perform(put("/api/users/1/name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateNameRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.lastName").value("Last name cannot be empty"));
    }

    @Test
    void testChangeNameInvalidFirstName() throws Exception {
        updateNameRequestDto.setFirstName("1234Name");
        userRepository.save(existingUser);

        mockMvc.perform(put("/api/users/1/name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateNameRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.firstName").value("Invalid name format"));
    }

    @Test
    void testChangeNameNotChanged() throws Exception {
        userRepository.save(existingUser);

        updateNameRequestDto.setFirstName("Jane");
        updateNameRequestDto.setLastName("Doe");

        mockMvc.perform(put("/api/users/1/name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateNameRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("No changes to first or last name were made"));
    }

    @Test
    void testDeleteUser() throws Exception {
        userRepository.save(existingUser);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        assertFalse(userRepository.existsById(existingUser.getId()));
    }

    @Test
    void testDeleteUserNotFound() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User with ID 1 not found"));
    }

    @Test
    void testGetUserEmailInfoSuccessfully() throws Exception {
        User user = userRepository.save(existingUser);
        int userId = user.getId();

        mockMvc.perform(get("/api/users/{id}/email-info", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(existingUser.getEmail()))
                .andExpect(jsonPath("$.verified").value(false));
    }

    @Test
    void testGetUserEmailInfoUserNotFound() throws Exception {
        mockMvc.perform(get("/api/users/1/email-info"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User with ID 1 not found"));
    }
}
