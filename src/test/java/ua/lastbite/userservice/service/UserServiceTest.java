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

import ua.lastbite.userservice.dto.email.EmailInfoResponseDto;
import ua.lastbite.userservice.dto.user.*;
import ua.lastbite.userservice.exception.user.*;
import ua.lastbite.userservice.mapper.UserResponseMapper ;
import ua.lastbite.userservice.mapper.UserRegistrationMapper;
import ua.lastbite.userservice.model.CountryCode;
import ua.lastbite.userservice.model.User;
import ua.lastbite.userservice.model.UserRole;
import ua.lastbite.userservice.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

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

    private static final long USER_ID = 1;

    @Test
    void testRegisterSuccessful() {

        UserRegistrationRequestDto request = new UserRegistrationRequestDto();
        request.setFirstName("Firstname");
        request.setLastName("Lastname");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setCountryCode(CountryCode.UA);
        request.setPhoneNumber("123456789");
        request.setRole(UserRole.CUSTOMER);

        User user = new User();
        user.setId(USER_ID);
        user.setFirstName("Firstname");
        user.setLastName("Lastname");
        user.setEmail("test@example.com");
        user.setRole(UserRole.CUSTOMER);

        UserResponseDto expectedResponse = new UserResponseDto(USER_ID, "Firstname", "Lastname", "test@example.com", UserRole.CUSTOMER);

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

        UserRegistrationRequestDto request = new UserRegistrationRequestDto();
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
        UserRegistrationRequestDto request = new UserRegistrationRequestDto();
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
        long userId2 = 2L;
        Pageable pageable = PageRequest.of(0, 10);

        User user1 = new User();
        user1.setId(USER_ID);
        user1.setFirstName("John");
        user1.setLastName("Doe");
        user1.setEmail("test1@example.com");

        User user2 = new User();
        user2.setId(userId2);
        user2.setFirstName("Jane");
        user2.setLastName("Doe");
        user2.setEmail("test2@example.com");

        List<User> users = List.of(user1, user2);
        Page<User> usersPage = new PageImpl<>(users, pageable, users.size());

        Mockito.when(userRepository.findAll(pageable)).thenReturn(usersPage);

        UserResponseDto userDto1 = new UserResponseDto(USER_ID, "John", "Doe", "test1@example.com", UserRole.CUSTOMER);
        UserResponseDto userDto2 = new UserResponseDto(userId2, "Jane", "Doe", "test2@example.com", UserRole.CUSTOMER);
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
        user.setId(USER_ID);
        user.setFirstName("Firstname");
        user.setLastName("Lastname");
        user.setEmail("test@example.com");
        user.setRole(UserRole.CUSTOMER);

        Mockito.when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        UserResponseDto userResponseDto = new UserResponseDto(USER_ID, "Firstname", "Lastname", "test@example.com", UserRole.CUSTOMER);
        Mockito.when(userResponseMapper.toUserResponseDto(user)).thenReturn(userResponseDto);

        UserResponseDto response = userService.getUserById(USER_ID);

        assertNotNull(response);
        assertEquals(USER_ID, response.getId());
        assertEquals("Firstname", response.getFirstName());
        assertEquals("Lastname", response.getLastName());
        assertEquals("test@example.com", response.getEmail());

        Mockito.verify(userRepository, Mockito.times(1)).findById(USER_ID);
        Mockito.verify(userResponseMapper, Mockito.times(1)).toUserResponseDto(user);
    }

    @Test
    void testGetUserByIdNotFound() {

        Mockito.when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        UserNotFoundException e = assertThrows(UserNotFoundException.class,
                () -> userService.getUserById(USER_ID));

        assertEquals("User with ID " + USER_ID + " not found", e.getMessage());

        Mockito.verify(userRepository, Mockito.times(1)).findById(USER_ID);
    }

    @Test
    void testDeleteUserSuccessfully() {
        User user = new User();
        user.setId(USER_ID);

        Mockito.when(userRepository.existsById(USER_ID)).thenReturn(true);
        userService.deleteUser(USER_ID);
        Mockito.verify(userRepository, Mockito.times(1)).deleteById(USER_ID);
    }

    @Test
    void testDeleteUserNotFound() {
        Mockito.when(userRepository.existsById(USER_ID)).thenReturn(false);

        UserNotFoundException e = assertThrows(UserNotFoundException.class,
                () -> userService.deleteUser(USER_ID));

        assertEquals("User with ID " + USER_ID + " not found", e.getMessage());
        Mockito.verify(userRepository, never()).deleteById(USER_ID);
    }

    @Test
    void testUpdateEmailAddressSuccessfully() {
        ChangeEmailRequestDto request = new ChangeEmailRequestDto();
        request.setNewEmail("new@example.com");

        User existingUser = new User();
        existingUser.setId(USER_ID);
        existingUser.setEmail("test@example.com");

        Mockito.when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
        Mockito.when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());

        userService.updateEmailAddress(USER_ID, request);

        Mockito.verify(userRepository, Mockito.times(1)).findById(USER_ID);
        Mockito.verify(userRepository, Mockito.times(1)).save(any(User.class));

        assertEquals("new@example.com", existingUser.getEmail());
        assertFalse(existingUser.isEmailVerified());
        assertNotNull(existingUser.getUpdatedAt());
    }

    @Test
    void testUpdateEmailAddressUserNotFound() {
        ChangeEmailRequestDto request = new ChangeEmailRequestDto();
        Mockito.when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.updateEmailAddress(USER_ID, request));

        assertEquals("User with ID " + USER_ID + " not found", exception.getMessage());

        Mockito.verify(userRepository, Mockito.times(1)).findById(USER_ID);
        Mockito.verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    void testUpdateEmailAddressNotChanged() {
        ChangeEmailRequestDto request = new ChangeEmailRequestDto();
        request.setNewEmail("test@example.com");

        User existingUser = new User();
        existingUser.setId(USER_ID);
        existingUser.setEmail("test@example.com");

        Mockito.when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));

        EmailNotChangedException exception = assertThrows(EmailNotChangedException.class,
                () -> userService.updateEmailAddress(USER_ID, request));

        assertEquals("New email is the same as the current email", exception.getMessage());
        assertNull(existingUser.getUpdatedAt());

        Mockito.verify(userRepository, Mockito.times(1)).findById(USER_ID);
        Mockito.verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    void testUpdateEmailAddressAlreadyExists() {
        long user2Id = 2;
        ChangeEmailRequestDto request = new ChangeEmailRequestDto();
        request.setNewEmail("new@example.com");

        User existingUser = new User();
        existingUser.setId(USER_ID);
        existingUser.setEmail("test@example.com");

        User anotherUser = new User();
        anotherUser.setId(user2Id);
        anotherUser.setEmail("new@example.com");

        Mockito.when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
        Mockito.when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.of(anotherUser));

        EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class,
                () -> userService.updateEmailAddress(USER_ID, request));

        assertEquals("Email new@example.com is already in use", exception.getMessage());
        assertNull(existingUser.getUpdatedAt());

        Mockito.verify(userRepository, Mockito.times(1)).findById(USER_ID);
        Mockito.verify(userRepository, Mockito.times(1)).findByEmail("new@example.com");
        Mockito.verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    void updatePasswordSuccessfully() {
        ChangePasswordRequestDto request = new ChangePasswordRequestDto();
        request.setCurrentPassword("currentPassword");
        request.setNewPassword("newPassword");

        User existingUser = new User();
        existingUser.setId(USER_ID);
        existingUser.setPassword(passwordEncoder.encode("currentPassword"));

        Mockito.when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
        Mockito.when(passwordEncoder.matches(request.getCurrentPassword(), existingUser.getPassword())).thenReturn(true);
        Mockito.when(passwordEncoder.encode(request.getNewPassword())).thenReturn("encodedNewPassword");

        userService.updatePassword(USER_ID, request);

        assertEquals("encodedNewPassword", existingUser.getPassword());
        assertNotNull(existingUser.getPasswordUpdatedAt());

        Mockito.verify(userRepository, Mockito.times(1)).findById(USER_ID);
        Mockito.verify(userRepository, Mockito.times(1)).save(any(User.class));
        Mockito.verify(passwordEncoder, Mockito.times(1)).encode(request.getNewPassword());
    }

    @Test
    void testUpdatePasswordUserNotFound() {
        ChangePasswordRequestDto request = new ChangePasswordRequestDto();

        Mockito.when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.updatePassword(USER_ID, request));

        assertEquals("User with ID " + USER_ID + " not found", exception.getMessage());

        Mockito.verify(userRepository, Mockito.times(1)).findById(USER_ID);
        Mockito.verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    void testUpdatePasswordIncorrectCurrentPassword() {
        ChangePasswordRequestDto request = new ChangePasswordRequestDto();
        request.setCurrentPassword("incorrectPassword");
        request.setNewPassword("newPassword");

        User existingUser = new User();
        existingUser.setId(USER_ID);
        existingUser.setPassword(passwordEncoder.encode("currentPassword"));

        Mockito.when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
        Mockito.when(passwordEncoder.matches(request.getCurrentPassword(), existingUser.getPassword())).thenReturn(false);

        IncorrectCurrentPasswordException exception = assertThrows(IncorrectCurrentPasswordException.class,
                () -> userService.updatePassword(USER_ID, request));

        assertEquals("Current password is incorrect", exception.getMessage());
        assertNull(existingUser.getPasswordUpdatedAt());

        Mockito.verify(userRepository, Mockito.times(1)).findById(USER_ID);
        Mockito.verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    void testUpdatePasswordNotChanged() {
        ChangePasswordRequestDto request = new ChangePasswordRequestDto();
        request.setCurrentPassword("currentPassword");
        request.setNewPassword("currentPassword");

        User existingUser = new User();
        existingUser.setId(USER_ID);
        existingUser.setPassword(passwordEncoder.encode("currentPassword"));

        Mockito.when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
        Mockito.when(passwordEncoder.matches(request.getNewPassword(), existingUser.getPassword())).thenReturn(true);

        PasswordNotChangedException exception = assertThrows(PasswordNotChangedException.class,
                () -> userService.updatePassword(USER_ID, request));

        assertEquals("New password cannot be the same as the current password", exception.getMessage());
        assertNull(existingUser.getPasswordUpdatedAt());

        Mockito.verify(userRepository, Mockito.times(1)).findById(USER_ID);
        Mockito.verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    void testUpdatePhoneNumberSuccessfully() {
        ChangePhoneNumberRequestDto request = new ChangePhoneNumberRequestDto();
        request.setCountryCode(CountryCode.UA);
        request.setNewPhoneNumber("987654321");

        User existingUser = new User();
        existingUser.setId(USER_ID);
        existingUser.setPhoneNumber("123456789");

        Mockito.when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
        Mockito.when(userRepository.findByPhoneNumber("987654321")).thenReturn(Optional.empty());

        userService.updatePhoneNumber(USER_ID, request);

        assertEquals("987654321", existingUser.getPhoneNumber());
        assertNotNull(existingUser.getUpdatedAt());

        Mockito.verify(userRepository, Mockito.times(1)).findById(USER_ID);
        Mockito.verify(userRepository, Mockito.times(1)).findByPhoneNumber("987654321");
        Mockito.verify(userRepository, Mockito.times(1)).save(any(User.class));
    }

    @Test
    void testUpdatePhoneNumberUserNotFound() {
        ChangePhoneNumberRequestDto request = new ChangePhoneNumberRequestDto();

        Mockito.when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.updatePhoneNumber(USER_ID, request));

        assertEquals("User with ID " + USER_ID + " not found", exception.getMessage());

        Mockito.verify(userRepository, Mockito.times(1)).findById(USER_ID);
        Mockito.verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    void testUpdatePhoneNumberNotChanged() {
        ChangePhoneNumberRequestDto request = new ChangePhoneNumberRequestDto();
        request.setCountryCode(CountryCode.UA);
        request.setNewPhoneNumber("123456789");

        User existingUser = new User();
        existingUser.setId(USER_ID);
        existingUser.setCountryCode(CountryCode.UA);
        existingUser.setPhoneNumber("123456789");

        Mockito.when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));

        PhoneNumberNotChangedException exception = assertThrows(PhoneNumberNotChangedException.class,
                () -> userService.updatePhoneNumber(USER_ID, request));

        assertEquals("New phone number cannot be the same as the current phone number", exception.getMessage());
        assertNull(existingUser.getUpdatedAt());

        Mockito.verify(userRepository, Mockito.times(1)).findById(USER_ID);
        Mockito.verify(userRepository, Mockito.never()).findByPhoneNumber(anyString());
        Mockito.verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    void testUpdatePhoneNumberAlreadyExist() {
        long user2Id = 2L;
        ChangePhoneNumberRequestDto request = new ChangePhoneNumberRequestDto();
        request.setCountryCode(CountryCode.UA);
        request.setNewPhoneNumber("987654321");

        User existingUser = new User();
        existingUser.setId(USER_ID);
        existingUser.setCountryCode(CountryCode.UA);
        existingUser.setPhoneNumber("123456789");

        User anotherUser = new User();
        anotherUser.setId(user2Id);
        anotherUser.setCountryCode(CountryCode.UA);
        anotherUser.setPhoneNumber("987654321");

        Mockito.when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
        Mockito.when(userRepository.findByPhoneNumber("987654321")).thenReturn(Optional.of(anotherUser));

        PhoneNumberAlreadyExistsException exception = assertThrows(PhoneNumberAlreadyExistsException.class,
                () -> userService.updatePhoneNumber(USER_ID, request));

        assertEquals("Phone number 987654321 is already in use", exception.getMessage());
        assertNull(existingUser.getUpdatedAt());

        Mockito.verify(userRepository, Mockito.times(1)).findById(USER_ID);
        Mockito.verify(userRepository, Mockito.times(1)).findByPhoneNumber(anyString());
        Mockito.verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    void testUpdateNameSuccessfully() {
        UpdateNameRequestDto request = new UpdateNameRequestDto();
        request.setFirstName("Firstname");
        request.setLastName("LastName");

        User existingUser = new User();
        existingUser.setId(USER_ID);
        existingUser.setFirstName("John");
        existingUser.setLastName("Doe");

        Mockito.when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));

        userService.updateName(USER_ID, request);

        Mockito.verify(userRepository, Mockito.times(1)).findById(USER_ID);
        Mockito.verify(userRepository, Mockito.times(1)).save(any(User.class));

        assertEquals("Firstname", existingUser.getFirstName());
        assertEquals("LastName", existingUser.getLastName());
        assertNotNull(existingUser.getUpdatedAt());
    }

    @Test
    void testUpdateNameUserNotFound() {
        UpdateNameRequestDto request = new UpdateNameRequestDto();
        Mockito.when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        UserNotFoundException e = assertThrows(UserNotFoundException.class,
                () -> userService.updateName(USER_ID, request));

        assertEquals("User with ID " + USER_ID + " not found", e.getMessage());

        Mockito.verify(userRepository, Mockito.times(1)).findById(USER_ID);
        Mockito.verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    void testUpdateNameNotChanged() {
        UpdateNameRequestDto request = new UpdateNameRequestDto();
        request.setFirstName("Firstname");
        request.setLastName("LastName");

        User existingUser = new User();
        existingUser.setId(USER_ID);
        existingUser.setFirstName("Firstname");
        existingUser.setLastName("LastName");

        Mockito.when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));

        NameNotChangedException exception = assertThrows(NameNotChangedException.class,
                () -> userService.updateName(USER_ID, request));

        assertEquals("No changes to first or last name were made", exception.getMessage());
        assertNull(existingUser.getUpdatedAt());

        Mockito.verify(userRepository, Mockito.times(1)).findById(USER_ID);
    }

    @Test
    void testGetUserEmailInfoSuccessfully() {

        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("email@example.com");

        Mockito.when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        EmailInfoResponseDto response = userService.getUserEmailInfo(USER_ID);

        Mockito.verify(userRepository, Mockito.times(1)).findById(USER_ID);

        assertNotNull(response);
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.isEmailVerified(), response.isVerified());
    }

    @Test
    void testGetUserEmailInfoUserNotFound() {

        Mockito.when(userRepository.findById(USER_ID))
                .thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.getUserEmailInfo(USER_ID));

        Mockito.verify(userRepository, Mockito.times(1)).findById(USER_ID);

        assertEquals("User with ID " + USER_ID + " not found", exception.getMessage());
    }
}
