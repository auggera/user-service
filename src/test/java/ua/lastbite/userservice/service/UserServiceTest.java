package ua.lastbite.userservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import ua.lastbite.userservice.dto.*;
import ua.lastbite.userservice.exception.user.*;
import ua.lastbite.userservice.mapper.UserResponseMapper ;
import ua.lastbite.userservice.mapper.UserRegistrationMapper;
import ua.lastbite.userservice.model.CountryCode;
import ua.lastbite.userservice.model.User;
import ua.lastbite.userservice.model.UserRole;
import ua.lastbite.userservice.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Mock
    private UserResponseMapper userResponseMapper ;

    @Mock
    private UserRegistrationMapper registrationMapper;

    @Test
    void testRegisterSuccessful() {

        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setFirstName("Firstname");
        request.setLastName("Lastname");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setCountryCode(CountryCode.UA);
        request.setPhoneNumber("123456789");
        request.setRole(UserRole.CUSTOMER);

        User user = new User();
        user.setId(1);
        user.setFirstName("Firstname");
        user.setLastName("Lastname");
        user.setEmail("test@example.com");
        user.setRole(UserRole.CUSTOMER);

        UserResponseDto expectedResponse = new UserResponseDto(1, "Firstname", "Lastname", "test@example.com", UserRole.CUSTOMER);

        Mockito.when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        Mockito.when(userRepository.findByPhoneNumber("123456789")).thenReturn(Optional.empty());
        Mockito.when(registrationMapper.toUser(request)).thenReturn(user);
        Mockito.when(passwordEncoder.encode("password123")).thenReturn("hashedPassword123");
        Mockito.when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponseDto response = userService.register(request);

        assertNotNull(response);
        assertEquals(expectedResponse.getId(), response.getId());
        assertEquals(expectedResponse.getFirstName(), response.getFirstName());
        assertEquals(expectedResponse.getLastName(), response.getLastName());
        assertEquals(expectedResponse.getEmail(), response.getEmail());
        assertEquals(expectedResponse.getRole(), response.getRole());

        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(userRepository, times(1)).findByPhoneNumber("123456789");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(user);
        verify(registrationMapper, times(1)).toUser(request);
    }

    @Test
    void testRegisterDuplicateEmail() {

        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setFirstName("Firstname");
        request.setLastName("Lastname");
        request.setEmail("test@example.com");
        request.setPhoneNumber("123456789");

        Mockito.when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(new User()));

        EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class,
                () -> userService.register(request));

        assertEquals("Email test@example.com is already in use", exception.getMessage());

        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(userRepository, never()).findByPhoneNumber(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegisterDuplicatePhoneNumber() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setFirstName("Firstname");
        request.setLastName("Lastname");
        request.setEmail("test@example.com");
        request.setPhoneNumber("123456789");

        Mockito.when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        Mockito.when(userRepository.findByPhoneNumber("123456789")).thenReturn(Optional.of(new User()));

        PhoneNumberAlreadyExistsException exception = assertThrows(PhoneNumberAlreadyExistsException.class,
                () -> userService.register(request));

        assertEquals("Phone number 123456789 is already in use", exception.getMessage());

        verify(userRepository, times(1)).findByPhoneNumber("123456789");
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void testGetAllUsers() {
        Pageable pageable = PageRequest.of(0, 10);

        User user1 = new User();
        user1.setId(1);
        user1.setFirstName("John");
        user1.setLastName("Doe");
        user1.setEmail("test1@example.com");

        User user2 = new User();
        user2.setId(2);
        user2.setFirstName("Jane");
        user2.setLastName("Doe");
        user2.setEmail("test2@example.com");

        List<User> users = List.of(user1, user2);
        Page<User> usersPage = new PageImpl<>(users, pageable, users.size());

        Mockito.when(userRepository.findAll(pageable)).thenReturn(usersPage);

        UserResponseDto userDto1 = new UserResponseDto(1, "John", "Doe", "test1@example.com", UserRole.CUSTOMER);
        UserResponseDto userDto2 = new UserResponseDto(2, "Jane", "Doe", "test2@example.com", UserRole.CUSTOMER);
        Page<UserResponseDto> usersDtoPage = new PageImpl<>(List.of(userDto1, userDto2), pageable, users.size());

        Mockito.when(userResponseMapper.toUserResponseDtoPage(usersPage)).thenReturn(usersDtoPage);

        Page<UserResponseDto> result = userService.getAllUsers(pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getContent().get(0).getId());
        assertEquals("John", result.getContent().get(0).getFirstName());
        assertEquals("Doe", result.getContent().get(0).getLastName());
        assertEquals("test2@example.com", result.getContent().get(1).getEmail());

        verify(userRepository, times(1)).findAll(pageable);
        verify(userResponseMapper, times(1)).toUserResponseDtoPage(usersPage);
    }

    @Test
    void testGetUserByIdSuccessful() {
        User user = new User();
        user.setId(1);
        user.setFirstName("Firstname");
        user.setLastName("Lastname");
        user.setEmail("test@example.com");
        user.setRole(UserRole.CUSTOMER);

        Mockito.when(userRepository.findById(1)).thenReturn(Optional.of(user));

        UserResponseDto userResponseDto = new UserResponseDto(1, "Firstname", "Lastname", "test@example.com", UserRole.CUSTOMER);
        Mockito.when(userResponseMapper.toUserResponseDto(user)).thenReturn(userResponseDto);

        UserResponseDto response = userService.getUserById(1);

        assertNotNull(response);
        assertEquals(1, response.getId());
        assertEquals("Firstname", response.getFirstName());
        assertEquals("Lastname", response.getLastName());
        assertEquals("test@example.com", response.getEmail());

        Mockito.verify(userRepository, Mockito.times(1)).findById(1);
        Mockito.verify(userResponseMapper, Mockito.times(1)).toUserResponseDto(user);
    }

    @Test
    void testGetUserByIdNotFound() {

        Mockito.when(userRepository.findById(1)).thenReturn(Optional.empty());

        UserNotFoundException e = assertThrows(UserNotFoundException.class,
                () -> userService.getUserById(1));

        assertEquals("User with ID 1 not found", e.getMessage());

        Mockito.verify(userRepository, Mockito.times(1)).findById(1);
    }

    @Test
    void testDeleteUserSuccessfully() {
        User user = new User();
        user.setId(1);

        Mockito.when(userRepository.existsById(1)).thenReturn(true);
        userService.deleteUser(1);
        Mockito.verify(userRepository, Mockito.times(1)).deleteById(1);
    }

    @Test
    void testDeleteUserNotFound() {
        Mockito.when(userRepository.existsById(1)).thenReturn(false);

        UserNotFoundException e = assertThrows(UserNotFoundException.class,
                () -> userService.deleteUser(1));

        assertEquals("User with ID 1 not found", e.getMessage());
        Mockito.verify(userRepository, never()).deleteById(1);
    }

    @Test
    void testUpdateEmailAddressSuccessfully() {
        ChangeEmailRequest request = new ChangeEmailRequest();
        request.setNewEmail("new@example.com");

        User existingUser = new User();
        existingUser.setId(1);
        existingUser.setEmail("test@example.com");

        Mockito.when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));
        Mockito.when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());

        userService.updateEmailAddress(1, request);

        Mockito.verify(userRepository, Mockito.times(1)).findById(1);
        Mockito.verify(userRepository, Mockito.times(1)).save(any(User.class));

        assertEquals("new@example.com", existingUser.getEmail());
        assertFalse(existingUser.isEmailVerified());
        assertNotNull(existingUser.getUpdatedAt());
    }

    @Test
    void testUpdateEmailAddressUserNotFound() {
        ChangeEmailRequest request = new ChangeEmailRequest();
        Mockito.when(userRepository.findById(1)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.updateEmailAddress(1, request));

        assertEquals("User with ID 1 not found", exception.getMessage());

        Mockito.verify(userRepository, Mockito.times(1)).findById(1);
        Mockito.verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    void testUpdateEmailAddressNotChanged() {
        ChangeEmailRequest request = new ChangeEmailRequest();
        request.setNewEmail("test@example.com");

        User existingUser = new User();
        existingUser.setId(1);
        existingUser.setEmail("test@example.com");

        Mockito.when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));

        EmailAddressNotChangedException exception = assertThrows(EmailAddressNotChangedException.class,
                () -> userService.updateEmailAddress(1, request));

        assertEquals("New email is the same as the current email", exception.getMessage());
        assertNull(existingUser.getUpdatedAt());

        Mockito.verify(userRepository, Mockito.times(1)).findById(1);
        Mockito.verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    void testUpdateEmailAddressAlreadyExists() {
        ChangeEmailRequest request = new ChangeEmailRequest();
        request.setNewEmail("new@example.com");

        User existingUser = new User();
        existingUser.setId(1);
        existingUser.setEmail("test@example.com");

        User anotherUser = new User();
        anotherUser.setId(2);
        anotherUser.setEmail("new@example.com");

        Mockito.when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));
        Mockito.when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.of(anotherUser));

        EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class,
                () -> userService.updateEmailAddress(1, request));

        assertEquals("Email new@example.com is already in use", exception.getMessage());
        assertNull(existingUser.getUpdatedAt());

        Mockito.verify(userRepository, Mockito.times(1)).findById(1);
        Mockito.verify(userRepository, Mockito.times(1)).findByEmail("new@example.com");
        Mockito.verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    void updatePasswordSuccessfully() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("currentPassword");
        request.setNewPassword("newPassword");

        User existingUser = new User();
        existingUser.setId(1);
        existingUser.setPassword(passwordEncoder.encode("currentPassword"));

        Mockito.when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));
        Mockito.when(passwordEncoder.matches(request.getCurrentPassword(), existingUser.getPassword())).thenReturn(true);
        Mockito.when(passwordEncoder.encode(request.getNewPassword())).thenReturn("encodedNewPassword");

        userService.updatePassword(1, request);

        assertEquals("encodedNewPassword", existingUser.getPassword());
        assertNotNull(existingUser.getPasswordUpdatedAt());

        Mockito.verify(userRepository, Mockito.times(1)).findById(1);
        Mockito.verify(userRepository, Mockito.times(1)).save(any(User.class));
        Mockito.verify(passwordEncoder, Mockito.times(1)).encode(request.getNewPassword());
    }

    @Test
    void testUpdatePasswordUserNotFound() {
        ChangePasswordRequest request = new ChangePasswordRequest();

        Mockito.when(userRepository.findById(1)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.updatePassword(1, request));

        assertEquals("User with ID 1 not found", exception.getMessage());

        Mockito.verify(userRepository, Mockito.times(1)).findById(1);
        Mockito.verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    void testUpdatePasswordIncorrectCurrentPassword() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("incorrectPassword");
        request.setNewPassword("newPassword");

        User existingUser = new User();
        existingUser.setId(1);
        existingUser.setPassword(passwordEncoder.encode("currentPassword"));

        Mockito.when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));
        Mockito.when(passwordEncoder.matches(request.getCurrentPassword(), existingUser.getPassword())).thenReturn(false);

        IncorrectCurrentPasswordException exception = assertThrows(IncorrectCurrentPasswordException.class,
                () -> userService.updatePassword(1, request));

        assertEquals("Current password is incorrect", exception.getMessage());
        assertNull(existingUser.getPasswordUpdatedAt());

        Mockito.verify(userRepository, Mockito.times(1)).findById(1);
        Mockito.verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    void testUpdatePasswordNotChanged() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("currentPassword");
        request.setNewPassword("currentPassword");

        User existingUser = new User();
        existingUser.setId(1);
        existingUser.setPassword(passwordEncoder.encode("currentPassword"));

        Mockito.when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));
        Mockito.when(passwordEncoder.matches(request.getNewPassword(), existingUser.getPassword())).thenReturn(true);

        PasswordNotChangedException exception = assertThrows(PasswordNotChangedException.class,
                () -> userService.updatePassword(1, request));

        assertEquals("New password cannot be the same as the current password", exception.getMessage());
        assertNull(existingUser.getPasswordUpdatedAt());

        Mockito.verify(userRepository, Mockito.times(1)).findById(1);
        Mockito.verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    void testUpdatePhoneNumberSuccessfully() {
        ChangePhoneNumberRequest request = new ChangePhoneNumberRequest();
        request.setCountryCode(CountryCode.UA);
        request.setNewPhoneNumber("987654321");

        User existingUser = new User();
        existingUser.setId(1);
        existingUser.setPhoneNumber("123456789");

        Mockito.when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));
        Mockito.when(userRepository.findByPhoneNumber("987654321")).thenReturn(Optional.empty());

        userService.updatePhoneNumber(1, request);

        assertEquals("987654321", existingUser.getPhoneNumber());
        assertNotNull(existingUser.getUpdatedAt());

        Mockito.verify(userRepository, Mockito.times(1)).findById(1);
        Mockito.verify(userRepository, Mockito.times(1)).findByPhoneNumber("987654321");
        Mockito.verify(userRepository, Mockito.times(1)).save(any(User.class));
    }

    @Test
    void testUpdatePhoneNumberUserNotFound() {
        ChangePhoneNumberRequest request = new ChangePhoneNumberRequest();

        Mockito.when(userRepository.findById(1)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.updatePhoneNumber(1, request));

        assertEquals("User with ID 1 not found", exception.getMessage());

        Mockito.verify(userRepository, Mockito.times(1)).findById(1);
        Mockito.verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    void testUpdatePhoneNumberNotChanged() {
        ChangePhoneNumberRequest request = new ChangePhoneNumberRequest();
        request.setCountryCode(CountryCode.UA);
        request.setNewPhoneNumber("123456789");

        User existingUser = new User();
        existingUser.setId(1);
        existingUser.setCountryCode(CountryCode.UA);
        existingUser.setPhoneNumber("123456789");

        Mockito.when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));

        PhoneNumberNotChangedException exception = assertThrows(PhoneNumberNotChangedException.class,
                () -> userService.updatePhoneNumber(1, request));

        assertEquals("New phone number cannot be the same as the current phone number", exception.getMessage());
        assertNull(existingUser.getUpdatedAt());

        Mockito.verify(userRepository, Mockito.times(1)).findById(1);
        Mockito.verify(userRepository, Mockito.never()).findByPhoneNumber(anyString());
        Mockito.verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    void testUpdatePhoneNumberAlreadyExist() {
        ChangePhoneNumberRequest request = new ChangePhoneNumberRequest();
        request.setCountryCode(CountryCode.UA);
        request.setNewPhoneNumber("987654321");

        User existingUser = new User();
        existingUser.setId(1);
        existingUser.setCountryCode(CountryCode.UA);
        existingUser.setPhoneNumber("123456789");

        User anotherUser = new User();
        anotherUser.setId(2);
        anotherUser.setCountryCode(CountryCode.UA);
        anotherUser.setPhoneNumber("987654321");

        Mockito.when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));
        Mockito.when(userRepository.findByPhoneNumber("987654321")).thenReturn(Optional.of(anotherUser));

        PhoneNumberAlreadyExistsException exception = assertThrows(PhoneNumberAlreadyExistsException.class,
                () -> userService.updatePhoneNumber(1, request));

        assertEquals("Phone number 987654321 is already in use", exception.getMessage());
        assertNull(existingUser.getUpdatedAt());

        Mockito.verify(userRepository, Mockito.times(1)).findById(1);
        Mockito.verify(userRepository, Mockito.times(1)).findByPhoneNumber(anyString());
        Mockito.verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    void testUpdateNameSuccessfully() {
        ChangeNameRequest request = new ChangeNameRequest();
        request.setFirstName("Firstname");
        request.setLastName("LastName");

        User existingUser = new User();
        existingUser.setId(1);
        existingUser.setFirstName("John");
        existingUser.setLastName("Doe");

        Mockito.when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));

        userService.updateName(1, request);

        Mockito.verify(userRepository, Mockito.times(1)).findById(1);
        Mockito.verify(userRepository, Mockito.times(1)).save(any(User.class));

        assertEquals("Firstname", existingUser.getFirstName());
        assertEquals("LastName", existingUser.getLastName());
        assertNotNull(existingUser.getUpdatedAt());
    }

    @Test
    void testUpdateNameUserNotFound() {
        ChangeNameRequest request = new ChangeNameRequest();
        Mockito.when(userRepository.findById(1)).thenReturn(Optional.empty());

        UserNotFoundException e = assertThrows(UserNotFoundException.class,
                () -> userService.updateName(1, request));

        assertEquals("User with ID 1 not found", e.getMessage());

        Mockito.verify(userRepository, Mockito.times(1)).findById(1);
        Mockito.verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    void testUpdateNameNotChanged() {
        ChangeNameRequest request = new ChangeNameRequest();
        request.setFirstName("Firstname");
        request.setLastName("LastName");

        User existingUser = new User();
        existingUser.setId(1);
        existingUser.setFirstName("Firstname");
        existingUser.setLastName("LastName");

        Mockito.when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));

        NameNotChangedException exception = assertThrows(NameNotChangedException.class,
                () -> userService.updateName(1, request));

        assertEquals("No changes to first or last name were made", exception.getMessage());
        assertNull(existingUser.getUpdatedAt());

        Mockito.verify(userRepository, Mockito.times(1)).findById(1);
    }
}
