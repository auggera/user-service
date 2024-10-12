package ua.lastbite.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import ua.lastbite.userservice.dto.user.*;
import ua.lastbite.userservice.exception.user.*;
import ua.lastbite.userservice.model.CountryCode;
import ua.lastbite.userservice.model.UserRole;
import ua.lastbite.userservice.service.UserService;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;


    UserRegistrationRequest userRegistrationRequest;
    UserResponseDto userResponseDto;
    ChangeEmailRequest changeEmailRequest;
    ChangePasswordRequest changePasswordRequest;
    ChangePhoneNumberRequest changePhoneNumberRequest;
    ChangeNameRequest changeNameRequest;

    @BeforeEach
    void setUpUserRegistrationRequest() {
        userRegistrationRequest = new UserRegistrationRequest();
        userRegistrationRequest.setFirstName("John");
        userRegistrationRequest.setLastName("Doe");
        userRegistrationRequest.setEmail("john@example.com");
        userRegistrationRequest.setPassword("password123");
        userRegistrationRequest.setCountryCode(CountryCode.UA);
        userRegistrationRequest.setPhoneNumber("123456789");
        userRegistrationRequest.setRole(UserRole.CUSTOMER);

        userResponseDto = new UserResponseDto();
        userResponseDto.setId(1);
        userResponseDto.setFirstName("John");
        userResponseDto.setLastName("Doe");
        userResponseDto.setEmail("john@example.com");
        userResponseDto.setRole(UserRole.CUSTOMER);
    }

    @Test
    void testRegisterUserSuccessfully() throws Exception {
        Mockito.when(userService.register(userRegistrationRequest))
                .thenReturn(userResponseDto);

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRegistrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.role").value(UserRole.CUSTOMER.name()));

    }

    @Test
    void testRegisterUserEmailAlreadyExistException() throws Exception {
        Mockito.doThrow(new EmailAlreadyExistsException(userRegistrationRequest.getEmail()))
                .when(userService).register(userRegistrationRequest);

        mockMvc.perform(post("/api/users/register")
                .content(objectMapper.writeValueAsString(userRegistrationRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email " + userRegistrationRequest.getEmail() + " is already in use"));
    }

    @Test
    void testRegisterUserPhoneNumberAlreadyExistException() throws Exception {
        Mockito.doThrow(new PhoneNumberAlreadyExistsException(userRegistrationRequest.getPhoneNumber()))
                .when(userService).register(userRegistrationRequest);

        mockMvc.perform(post("/api/users/register")
                .content(objectMapper.writeValueAsString(userRegistrationRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Phone number " + userRegistrationRequest.getPhoneNumber() + " is already in use"));
    }

    @Test
    void testGetAllUsers() throws Exception {
        List<UserResponseDto> users = Arrays.asList(
                new UserResponseDto(1, "John", "Doe", "john@example.com", UserRole.CUSTOMER),
                new UserResponseDto(2, "Jane", "Doe", "jane@example.com", UserRole.CUSTOMER)
        );
        Page<UserResponseDto> userPage = new PageImpl<>(users);

        Mockito.when(userService.getAllUsers(Mockito.any(PageRequest.class)))
                .thenReturn(userPage);

        mockMvc.perform(get("/api/users")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(users.size()))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[0].email").value("john@example.com"))
                .andExpect(jsonPath("$[1].firstName").value("Jane"))
                .andExpect(jsonPath("$[1].email").value("jane@example.com"));
    }

    @Test
    void testGetUserByIdSuccessfully() throws Exception {
        Mockito.when(userService.getUserById(1))
                .thenReturn(userResponseDto);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void testGetUserByIdNotFoundException() throws Exception {
        Mockito.when(userService.getUserById(1))
                .thenThrow(new UserNotFoundException(1));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User with ID 1 not found"));
    }

    @BeforeEach
    void setUpChangeEmailRequest() {
        changeEmailRequest = new ChangeEmailRequest();
        changeEmailRequest.setNewEmail("new@example.com");
    }

    @Test
    void testChangeEmailSuccessfully() throws Exception {
        mockMvc.perform(put("/api/users/1/email")
            .content(objectMapper.writeValueAsString(changeEmailRequest))
            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void testChangeEmailUserNotFoundException() throws Exception {
        Mockito.doThrow(new UserNotFoundException(1))
                .when(userService).updateEmailAddress(1, changeEmailRequest);

        mockMvc.perform(put("/api/users/1/email")
                .content(objectMapper.writeValueAsString(changeEmailRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User with ID 1 not found"));
    }

    @Test
    void testChangeEmailAddressNotChangedException() throws Exception {
        Mockito.doThrow(new EmailAddressNotChangedException())
                .when(userService).updateEmailAddress(1, changeEmailRequest);

        mockMvc.perform(put("/api/users/1/email")
                .content(objectMapper.writeValueAsString(changeEmailRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("New email is the same as the current email"));
    }

    @Test
    void testChangeEmailAddressAlreadyExistException() throws Exception {
        Mockito.doThrow(new EmailAlreadyExistsException(changeEmailRequest.getNewEmail()))
                .when(userService).updateEmailAddress(1, changeEmailRequest);

        mockMvc.perform(put("/api/users/1/email")
                .content(objectMapper.writeValueAsString(changeEmailRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email " + changeEmailRequest.getNewEmail() + " is already in use"));
    }

    @BeforeEach
    void setUpChangePasswordRequest() {
        changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setCurrentPassword("currentPassword123");
        changePasswordRequest.setNewPassword("newPassword123");
    }

    @Test
    void testChangePasswordSuccessfully() throws Exception {
        mockMvc.perform(put("/api/users/1/password")
                .content(objectMapper.writeValueAsString(changePasswordRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void testChangePasswordUserNotFoundException() throws Exception {
        Mockito.doThrow(new UserNotFoundException(1))
                .when(userService).updatePassword(1, changePasswordRequest);

        mockMvc.perform(put("/api/users/1/password")
                .content(objectMapper.writeValueAsString(changePasswordRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User with ID 1 not found"));
    }

    @Test
    void testChangePasswordIncorrectCurrentPasswordException() throws Exception {
        Mockito.doThrow(new IncorrectCurrentPasswordException())
                .when(userService).updatePassword(1, changePasswordRequest);

        mockMvc.perform(put("/api/users/1/password" )
                .content(objectMapper.writeValueAsString(changePasswordRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Current password is incorrect"));
    }

    @Test
    void testChangePasswordNotChangedException() throws Exception {
        Mockito.doThrow(new PasswordNotChangedException())
                .when(userService).updatePassword(1, changePasswordRequest);

        mockMvc.perform(put("/api/users/1/password")
                .content(objectMapper.writeValueAsString(changePasswordRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("New password cannot be the same as the current password"));
    }

    @BeforeEach
    void setUpChangePhoneNumberRequest() {
        changePhoneNumberRequest = new ChangePhoneNumberRequest();
        changePhoneNumberRequest.setCountryCode(CountryCode.UA);
        changePhoneNumberRequest.setNewPhoneNumber("987654321");
    }

    @Test
    void testChangePhoneNumberSuccessfully() throws Exception {
        mockMvc.perform(put("/api/users/1/phone")
                .content(objectMapper.writeValueAsString(changePhoneNumberRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void testChangePhoneNumberUserNotFoundException() throws Exception {
        Mockito.doThrow(new UserNotFoundException(1))
                .when(userService).updatePhoneNumber(1, changePhoneNumberRequest);

        mockMvc.perform(put("/api/users/1/phone")
                .content(objectMapper.writeValueAsString(changePhoneNumberRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User with ID 1 not found"));
    }

    @Test
    void testChangePhoneNumberNotChangedException() throws Exception {
        Mockito.doThrow(new PhoneNumberNotChangedException())
                .when(userService).updatePhoneNumber(1, changePhoneNumberRequest);

        mockMvc.perform(put("/api/users/1/phone")
                .content(objectMapper.writeValueAsString(changePhoneNumberRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("New phone number cannot be the same as the current phone number"));
    }

    @BeforeEach
    void setUpChangeNameRequest() {
        changeNameRequest = new ChangeNameRequest();
        changeNameRequest.setFirstName("Davis");
        changeNameRequest.setLastName("Deutsch");
    }

    @Test
    void testChangeNameSuccessfully() throws Exception {
        mockMvc.perform(put("/api/users/1/name")
                .content(objectMapper.writeValueAsString(changeNameRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void testChangeNameUserNotFoundException() throws Exception {
        Mockito.doThrow(new UserNotFoundException(1))
                .when(userService).updateName(1, changeNameRequest);

        mockMvc.perform(put("/api/users/1/name")
                .content(objectMapper.writeValueAsString(changeNameRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User with ID 1 not found"));
    }

    @Test
    void testChangeNameNotChangedException() throws Exception {
        Mockito.doThrow(new NameNotChangedException())
                .when(userService).updateName(1, changeNameRequest);

        mockMvc.perform(put("/api/users/1/name")
                .content(objectMapper.writeValueAsString(changeNameRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("No changes to first or last name were made"));
    }

    @Test
    void testDeleteUserSuccessfully() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteUserNotFoundException() throws Exception {
        Mockito.doThrow(new UserNotFoundException(1))
                        .when(userService).deleteUser(1);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User with ID 1 not found"));
    }
}