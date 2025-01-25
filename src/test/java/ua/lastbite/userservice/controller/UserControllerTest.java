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

import ua.lastbite.userservice.dto.email.EmailInfoResponseDto;
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
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final long USER_ID = 1;
    UserRegistrationRequestDto userRegistrationRequestDto;
    UserResponseDto userResponseDto;
    ChangeEmailRequestDto changeEmailRequestDto;
    ChangePasswordRequestDto changePasswordRequestDto;
    ChangePhoneNumberRequestDto changePhoneNumberRequestDto;
    UpdateNameRequestDto updateNameRequestDto;
    EmailInfoResponseDto emailInfoResponseDto;

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

        userResponseDto = new UserResponseDto();
        userResponseDto.setId(USER_ID);
        userResponseDto.setFirstName("John");
        userResponseDto.setLastName("Doe");
        userResponseDto.setEmail("john@example.com");
        userResponseDto.setRole(UserRole.CUSTOMER);
    }

    @Test
    void testRegisterUserSuccessfully() throws Exception {
        Mockito.when(userService.register(userRegistrationRequestDto))
                .thenReturn(userResponseDto);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRegistrationRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.role").value(UserRole.CUSTOMER.name()));

    }

    @Test
    void testRegisterUserEmailAlreadyExistException() throws Exception {
        Mockito.doThrow(new EmailAlreadyExistsException(userRegistrationRequestDto.getEmail()))
                .when(userService).register(userRegistrationRequestDto);

        mockMvc.perform(post("/api/users")
                .content(objectMapper.writeValueAsString(userRegistrationRequestDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email " + userRegistrationRequestDto.getEmail() + " is already in use"));
    }

    @Test
    void testRegisterUserPhoneNumberAlreadyExistException() throws Exception {
        Mockito.doThrow(new PhoneNumberAlreadyExistsException(userRegistrationRequestDto.getPhoneNumber()))
                .when(userService).register(userRegistrationRequestDto);

        mockMvc.perform(post("/api/users")
                .content(objectMapper.writeValueAsString(userRegistrationRequestDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Phone number " + userRegistrationRequestDto.getPhoneNumber() + " is already in use"));
    }

    @Test
    void testGetAllUsers() throws Exception {
        List<UserResponseDto> users = Arrays.asList(
                new UserResponseDto(1L, "John", "Doe", "john@example.com", UserRole.CUSTOMER),
                new UserResponseDto(2L, "Jane", "Doe", "jane@example.com", UserRole.CUSTOMER)
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
        Mockito.when(userService.getUserById(USER_ID))
                .thenReturn(userResponseDto);

        mockMvc.perform(get("/api/users/{id}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void testGetUserByIdNotFoundException() throws Exception {
        Mockito.when(userService.getUserById(USER_ID))
                .thenThrow(new UserNotFoundException(USER_ID));

        mockMvc.perform(get("/api/users/{id}", USER_ID))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User with ID " + USER_ID + " not found"));
    }

    @BeforeEach
    void setUpChangeEmailRequest() {
        changeEmailRequestDto = new ChangeEmailRequestDto();
        changeEmailRequestDto.setNewEmail("new@example.com");
    }

    @Test
    void testChangeEmailSuccessfully() throws Exception {
        mockMvc.perform(put("/api/users/{id}/email", USER_ID)
            .content(objectMapper.writeValueAsString(changeEmailRequestDto))
            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void testChangeEmailUserNotFoundException() throws Exception {
        Mockito.doThrow(new UserNotFoundException(USER_ID))
                .when(userService).updateEmailAddress(USER_ID, changeEmailRequestDto);

        mockMvc.perform(put("/api/users/{id}/email", USER_ID)
                .content(objectMapper.writeValueAsString(changeEmailRequestDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User with ID " + USER_ID + " not found"));
    }

    @Test
    void testChangeEmailAddressNotChangedException() throws Exception {
        Mockito.doThrow(new EmailNotChangedException())
                .when(userService).updateEmailAddress(USER_ID, changeEmailRequestDto);

        mockMvc.perform(put("/api/users/{id}/email", USER_ID)
                .content(objectMapper.writeValueAsString(changeEmailRequestDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("New email is the same as the current email"));
    }

    @Test
    void testChangeEmailAddressAlreadyExistException() throws Exception {
        Mockito.doThrow(new EmailAlreadyExistsException(changeEmailRequestDto.getNewEmail()))
                .when(userService).updateEmailAddress(USER_ID, changeEmailRequestDto);

        mockMvc.perform(put("/api/users/{id}/email", USER_ID)
                .content(objectMapper.writeValueAsString(changeEmailRequestDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email " + changeEmailRequestDto.getNewEmail() + " is already in use"));
    }

    @BeforeEach
    void setUpChangePasswordRequest() {
        changePasswordRequestDto = new ChangePasswordRequestDto();
        changePasswordRequestDto.setCurrentPassword("currentPassword123");
        changePasswordRequestDto.setNewPassword("newPassword123");
    }

    @Test
    void testChangePasswordSuccessfully() throws Exception {
        mockMvc.perform(put("/api/users/{id}/password", USER_ID)
                .content(objectMapper.writeValueAsString(changePasswordRequestDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void testChangePasswordUserNotFoundException() throws Exception {
        Mockito.doThrow(new UserNotFoundException(USER_ID))
                .when(userService).updatePassword(USER_ID, changePasswordRequestDto);

        mockMvc.perform(put("/api/users/{id}/password", USER_ID)
                .content(objectMapper.writeValueAsString(changePasswordRequestDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User with ID " + USER_ID + " not found"));
    }

    @Test
    void testChangePasswordIncorrectCurrentPasswordException() throws Exception {
        Mockito.doThrow(new IncorrectCurrentPasswordException())
                .when(userService).updatePassword(USER_ID, changePasswordRequestDto);

        mockMvc.perform(put("/api/users/{id}/password", USER_ID )
                .content(objectMapper.writeValueAsString(changePasswordRequestDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Current password is incorrect"));
    }

    @Test
    void testChangePasswordNotChangedException() throws Exception {
        Mockito.doThrow(new PasswordNotChangedException())
                .when(userService).updatePassword(USER_ID, changePasswordRequestDto);

        mockMvc.perform(put("/api/users/{id}/password", USER_ID)
                .content(objectMapper.writeValueAsString(changePasswordRequestDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("New password cannot be the same as the current password"));
    }

    @BeforeEach
    void setUpChangePhoneNumberRequest() {
        changePhoneNumberRequestDto = new ChangePhoneNumberRequestDto();
        changePhoneNumberRequestDto.setCountryCode(CountryCode.UA);
        changePhoneNumberRequestDto.setNewPhoneNumber("987654321");
    }

    @Test
    void testChangePhoneNumberSuccessfully() throws Exception {
        mockMvc.perform(put("/api/users/{id}/phone", USER_ID)
                .content(objectMapper.writeValueAsString(changePhoneNumberRequestDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void testChangePhoneNumberUserNotFoundException() throws Exception {
        Mockito.doThrow(new UserNotFoundException(USER_ID))
                .when(userService).updatePhoneNumber(USER_ID, changePhoneNumberRequestDto);

        mockMvc.perform(put("/api/users/{id}/phone", USER_ID)
                .content(objectMapper.writeValueAsString(changePhoneNumberRequestDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User with ID " + USER_ID + " not found"));
    }

    @Test
    void testChangePhoneNumberNotChangedException() throws Exception {
        Mockito.doThrow(new PhoneNumberNotChangedException())
                .when(userService).updatePhoneNumber(USER_ID, changePhoneNumberRequestDto);

        mockMvc.perform(put("/api/users/{id}/phone", USER_ID)
                .content(objectMapper.writeValueAsString(changePhoneNumberRequestDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("New phone number cannot be the same as the current phone number"));
    }

    @BeforeEach
    void setUpUpdateNameRequest() {
        updateNameRequestDto = new UpdateNameRequestDto();
        updateNameRequestDto.setFirstName("Davis");
        updateNameRequestDto.setLastName("Deutsch");
    }

    @Test
    void testUpdateNameSuccessfully() throws Exception {
        mockMvc.perform(patch("/api/users/{id}/name", USER_ID)
                .content(objectMapper.writeValueAsString(updateNameRequestDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void testUpdateNameUserNotFoundException() throws Exception {
        Mockito.doThrow(new UserNotFoundException(USER_ID))
                .when(userService).updateName(USER_ID, updateNameRequestDto);

        mockMvc.perform(patch("/api/users/{id}/name", USER_ID)
                .content(objectMapper.writeValueAsString(updateNameRequestDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User with ID " + USER_ID + " not found"));
    }

    @Test
    void testUpdateNameNotChangedException() throws Exception {
        Mockito.doThrow(new NameNotChangedException())
                .when(userService).updateName(USER_ID, updateNameRequestDto);

        mockMvc.perform(patch("/api/users/{id}/name", USER_ID)
                .content(objectMapper.writeValueAsString(updateNameRequestDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("No changes to first or last name were made"));
    }

    @Test
    void testDeleteUserSuccessfully() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", USER_ID))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteUserNotFoundException() throws Exception {
        Mockito.doThrow(new UserNotFoundException(USER_ID))
                        .when(userService).deleteUser(USER_ID);

        mockMvc.perform(delete("/api/users/{id}", USER_ID))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User with ID " + USER_ID + " not found"));
    }

    @BeforeEach
    void setUp() {
        emailInfoResponseDto = new EmailInfoResponseDto();
        emailInfoResponseDto.setEmail("email@example.com");
        emailInfoResponseDto.setVerified(false);
    }

    @Test
    void testGetUserEmailInfoSuccessfully() throws Exception {

        Mockito.when(userService.getUserEmailInfo(USER_ID))
                .thenReturn(emailInfoResponseDto);

        mockMvc.perform(get("/api/users/{id}/email/info", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("email@example.com"))
                .andExpect(jsonPath("$.verified").value(false));
    }

    @Test
    void testGetUserEmailInfoUserNotFound() throws Exception {

        Mockito.when(userService.getUserEmailInfo(USER_ID))
                .thenThrow(new UserNotFoundException(USER_ID));

        mockMvc.perform(get("/api/users/{id}/email/info", USER_ID))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User with ID " + USER_ID + " not found"));
    }
}