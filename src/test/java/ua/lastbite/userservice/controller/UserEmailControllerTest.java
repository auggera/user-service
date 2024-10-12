package ua.lastbite.userservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ua.lastbite.userservice.dto.email.UserEmailResponseDto;
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

    private UserEmailResponseDto userEmailResponseDto;
    private static final int USER_ID = 1;

    @BeforeEach
    void setUp() {
        userEmailResponseDto = new UserEmailResponseDto();
        userEmailResponseDto.setEmail("email@example.com");
        userEmailResponseDto.setEmailVerified(false);
    }

    @Test
    void testGetUserEmailInfoSuccessfully() throws Exception {
        Mockito.when(userEmailService.getUserEmailInfo(USER_ID))
                .thenReturn(userEmailResponseDto);

        mockMvc.perform(get("/api/email/1/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("email@example.com"))
                .andExpect(jsonPath("$.emailVerified").value(false));
    }

    @Test
    void testGetUserEmailInfoUserNotFound() throws Exception {
        Mockito.when(userEmailService.getUserEmailInfo(USER_ID))
                .thenThrow(new UserNotFoundException(USER_ID));

        mockMvc.perform(get("/api/email/1/info"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User with ID " + USER_ID + " not found"));
    }
}
