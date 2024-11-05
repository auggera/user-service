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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ua.lastbite.userservice.dto.email.UserEmailResponseDto;
import ua.lastbite.userservice.dto.token.TokenValidationRequest;
import ua.lastbite.userservice.dto.token.TokenValidationResponse;
import ua.lastbite.userservice.exception.user.UserNotFoundException;
import ua.lastbite.userservice.service.UserEmailService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
public class UserEmailControllerTest {

    @MockBean
    private UserEmailService userEmailService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private static final int USER_ID = 1;
    private UserEmailResponseDto userEmailResponseDto;

    @BeforeEach
    void setUp() {
        userEmailResponseDto = new UserEmailResponseDto();
        userEmailResponseDto.setEmail("email@example.com");
        userEmailResponseDto.setVerified(false);
    }

    @Test
    void testGetUserEmailInfoSuccessfully() throws Exception {
        Mockito.when(userEmailService.getUserEmailInfo(USER_ID))
                .thenReturn(userEmailResponseDto);

        mockMvc.perform(get("/api/email/{userId}/info", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("email@example.com"))
                .andExpect(jsonPath("$.verified").value(false));
    }

    @Test
    void testGetUserEmailInfoUserNotFound() throws Exception {
        Mockito.when(userEmailService.getUserEmailInfo(USER_ID))
                .thenThrow(new UserNotFoundException(USER_ID));

        mockMvc.perform(get("/api/email/{userId}/info", USER_ID))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User with ID " + USER_ID + " not found"));
    }

    @Test
    void verifyEmailSuccess() throws Exception {

        Mockito.doNothing().when(userEmailService).verifyEmail(USER_ID);

        mockMvc.perform(post("/api/email/{userId}/verify-email", USER_ID))
                .andExpect(status().isOk())
                .andExpect(content().string("Email successfully verified"));
    }

    @Test
    void verifyEmailUserNotFound() throws Exception {

        Mockito.doThrow(new UserNotFoundException(USER_ID)).when(userEmailService).verifyEmail(USER_ID);

        mockMvc.perform(post("/api/email/{userId}/verify-email", USER_ID))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User with ID 1 not found"));
    }

}
