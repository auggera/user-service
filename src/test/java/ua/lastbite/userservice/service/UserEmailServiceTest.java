package ua.lastbite.userservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.lastbite.userservice.dto.email.UserEmailResponseDto;
import ua.lastbite.userservice.exception.user.UserNotFoundException;
import ua.lastbite.userservice.model.User;
import ua.lastbite.userservice.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UserEmailServiceTest {

    @InjectMocks
    private UserEmailService userEmailService;

    @Mock
    private UserRepository userRepository;

    private User user;
    private static final int USER_ID = 1;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("email@example.com");
    }

    @Test
    void testGetUserEmailInfoSuccessfully() {
        Mockito.when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        UserEmailResponseDto response = userEmailService.getUserEmailInfo(USER_ID);

        Mockito.verify(userRepository, Mockito.times(1)).findById(USER_ID);

        assertNotNull(response);
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.isEmailVerified(), response.isVerified());
    }

    @Test
    void testGetUserEmailInfoUserNotFound() {
        Mockito.when(userRepository.findById(USER_ID))
                .thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userEmailService.getUserEmailInfo(USER_ID));

        Mockito.verify(userRepository, Mockito.times(1)).findById(USER_ID);

        assertEquals("User with ID 1 not found", exception.getMessage());
    }
}
