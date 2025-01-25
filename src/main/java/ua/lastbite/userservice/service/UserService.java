package ua.lastbite.userservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ua.lastbite.userservice.dto.email.EmailInfoResponseDto;
import ua.lastbite.userservice.dto.user.*;
import ua.lastbite.userservice.exception.user.*;
import ua.lastbite.userservice.mapper.UserResponseMapper ;
import ua.lastbite.userservice.mapper.UserRegistrationMapper;
import ua.lastbite.userservice.model.CountryCode;
import ua.lastbite.userservice.model.User;
import ua.lastbite.userservice.repository.UserRepository;

import java.time.LocalDateTime;


@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserResponseMapper userResponseMapper;
    private final UserRegistrationMapper registrationMapper;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       UserResponseMapper userResponseMapper, UserRegistrationMapper registrationMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userResponseMapper = userResponseMapper;
        this.registrationMapper = registrationMapper;
    }

    public UserResponseDto register(UserRegistrationRequestDto request) {
        log.info("Attempting to register user with email: {}", request.getEmail());
        log.debug("Checking if email or phone number already exists: email={}, phone={}", request.getEmail(), request.getPhoneNumber());
        checkIfEmailExists(request.getEmail());
        checkIfPhoneNumberExists(request.getPhoneNumber());

        User user = registrationMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);
        return new UserResponseDto(savedUser.getId(), savedUser.getFirstName(), savedUser.getLastName(), savedUser.getEmail(), savedUser.getRole());
    }

    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        Page<User> usersPage = userRepository.findAll(pageable);
        log.info("Fetched {} users", usersPage.getTotalElements());
        return userResponseMapper.toUserResponseDtoPage(usersPage);
    }

    public UserResponseDto getUserById(Long id) {
        log.info("Fetching user by id: {}", id);
        User user = findById(id);

        return userResponseMapper.toUserResponseDto(user);
    }

    public void deleteUser(Long id) {
        log.info("Attempting to delete user with id: {}", id);
        if (!userRepository.existsById(id)) {
            log.error("User doesn't exists with id: {}", id);
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }

    public void updateEmailAddress(Long id, ChangeEmailRequestDto request) {
        log.info("Updating email address for user with id: {}", id);
        User existingUser = findById(id);

        String newEmail = request.getNewEmail();

        if (existingUser.getEmail().equals(newEmail)) {
            log.warn("Email not changed for user with id: {}", id);
            throw new EmailNotChangedException();
        }

        checkIfEmailExists(newEmail);

        existingUser.setEmail(newEmail);
        existingUser.setEmailVerified(false);
        existingUser.setUpdatedAt(LocalDateTime.now());
        userRepository.save(existingUser);
    }

    public void updatePassword(Long id, ChangePasswordRequestDto request) {
        log.info("Updating password for user with id: {}", id);
        User existingUser = findById(id);

        String currentPassword = request.getCurrentPassword();
        String newPassword = request.getNewPassword();

        if (!passwordEncoder.matches(currentPassword, existingUser.getPassword())) {
            log.error("Incorrect current password for user with id: {}", id);
            throw new IncorrectCurrentPasswordException();
        }

        if (passwordEncoder.matches(newPassword, existingUser.getPassword())) {
            log.warn("New password matches the old password for user with id: {}", id);
            throw new PasswordNotChangedException();
        }

        existingUser.setPassword(passwordEncoder.encode(newPassword));
        existingUser.setPasswordUpdatedAt(LocalDateTime.now());
        userRepository.save(existingUser);
    }

    public void updatePhoneNumber(Long id, ChangePhoneNumberRequestDto request) {
        log.info("Updating phone number for user with id: {}", id);
        User existingUser = findById(id);

        CountryCode countryCode = request.getCountryCode();
        String newPhoneNumber = request.getNewPhoneNumber();

        if (existingUser.getPhoneNumber().equals(newPhoneNumber)) {
            log.warn("Phone number not changed for user with id: {}", id);
            throw new PhoneNumberNotChangedException();
        }

        checkIfPhoneNumberExists(newPhoneNumber);

        if (!countryCode.equals(existingUser.getCountryCode())) {
            existingUser.setCountryCode(countryCode);
        }
        existingUser.setPhoneNumber(newPhoneNumber);
        existingUser.setPhoneVerified(false);
        existingUser.setUpdatedAt(LocalDateTime.now());
        userRepository.save(existingUser);
    }

    public void updateName(Long id, UpdateNameRequestDto request) {
        log.info("Updating name for user with id: {}", id);
        User existingUser = findById(id);

        boolean isUpdated = false;

        if (isUpdateRequired(existingUser.getFirstName(), request.getFirstName())) {
            existingUser.setFirstName(request.getFirstName());
            isUpdated = true;
        }

        if (isUpdateRequired(existingUser.getLastName(), request.getLastName())) {
            existingUser.setLastName(request.getLastName());
            isUpdated = true;
        }

        if (!isUpdated) {
            log.warn("Name not changed for user with id: {}", id);
            throw new NameNotChangedException();
        }

        existingUser.setUpdatedAt(LocalDateTime.now());
        userRepository.save(existingUser);
    }

    private boolean isUpdateRequired(String existingName, String newName) {
        return newName != null && !existingName.equals(newName);
    }

    public EmailInfoResponseDto getUserEmailInfo(Long id) {
        log.info("Fetching email info for user with id: {}", id);
        User user = findById(id);

        return new EmailInfoResponseDto(user.getEmail(), user.isEmailVerified());
    }

    public void markEmailAsVerified(Long id) {
        log.info("Marking email as verified for user with id: {}", id);
        User user = findById(id);

        user.setEmailVerified(true);
        userRepository.save(user);
    }

    private User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", id);
                    return new UserNotFoundException(id);
                });
    }

    private void checkIfEmailExists(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            log.error("Email already exists: {}", email);
            throw new EmailAlreadyExistsException(email);
        }
    }

    private void checkIfPhoneNumberExists(String phoneNumber) {
        if (userRepository.findByPhoneNumber(phoneNumber).isPresent()) {
            log.error("Phone number already exists: {}", phoneNumber);
            throw new PhoneNumberAlreadyExistsException(phoneNumber);
        }
    }
}
