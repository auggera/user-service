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
    private TokenValidationRequest tokenValidationRequest;

    @BeforeEach
    void setUp() {
        userEmailResponseDto = new UserEmailResponseDto();
        userEmailResponseDto.setEmail("email@example.com");
        userEmailResponseDto.setVerified(false);

        tokenValidationRequest = new TokenValidationRequest("tokenValue123");
    }

    @Test
    void testGetUserEmailInfoSuccessfully() throws Exception {
        Mockito.when(userEmailService.getUserEmailInfo(USER_ID))
                .thenReturn(userEmailResponseDto);

        mockMvc.perform(get("/api/email/1/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("email@example.com"))
                .andExpect(jsonPath("$.verified").value(false));
    }

    @Test
    void testGetUserEmailInfoUserNotFound() throws Exception {
        Mockito.when(userEmailService.getUserEmailInfo(USER_ID))
                .thenThrow(new UserNotFoundException(USER_ID));

        mockMvc.perform(get("/api/email/1/info"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User with ID " + USER_ID + " not found"));
    }

    @Test
    void testVerifyEmailSuccessfully() throws Exception {

        mockMvc.perform(post("/api/email/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tokenValidationRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testVerifyEmailNullRequest() throws Exception {
        tokenValidationRequest = null;

        mockMvc.perform(post("/api/email/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenValidationRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testVerifyEmailTokenValueIsNull() throws Exception {
        tokenValidationRequest.setTokenValue(null);

        mockMvc.perform(post("/api/email/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenValidationRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.tokenValue").value("Token cannot be empty"));
    }
}
