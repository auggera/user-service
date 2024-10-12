package ua.lastbite.userservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ua.lastbite.userservice.dto.user.*;
import ua.lastbite.userservice.exception.user.*;
import ua.lastbite.userservice.mapper.UserResponseMapper ;
import ua.lastbite.userservice.mapper.UserRegistrationMapper;
import ua.lastbite.userservice.model.CountryCode;
import ua.lastbite.userservice.model.User;
import ua.lastbite.userservice.repository.UserRepository;

import java.time.LocalDateTime;


@Service
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

    public UserResponseDto register(UserRegistrationRequest request) {
        checkIfEmailOrPhoneExists(request);

        User user = registrationMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);
        return new UserResponseDto(savedUser.getId(), savedUser.getFirstName(), savedUser.getLastName(), savedUser.getEmail(), savedUser.getRole());
    }

    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        Page<User> usersPage = userRepository.findAll(pageable);
        return userResponseMapper.toUserResponseDtoPage(usersPage);
    }

    public UserResponseDto getUserById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        return userResponseMapper.toUserResponseDto(user);
    }

    public void deleteUser(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }

    public void updateEmailAddress(Integer id, ChangeEmailRequest request) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        String newEmail = request.getNewEmail();

        if (existingUser.getEmail().equals(newEmail)) {
            throw new EmailAddressNotChangedException();
        }

        if (userRepository.findByEmail(newEmail).isPresent()) {
            throw new EmailAlreadyExistsException(newEmail);
        }

        existingUser.setEmail(newEmail);
        existingUser.setEmailVerified(false);
        existingUser.setUpdatedAt(LocalDateTime.now());
        userRepository.save(existingUser);
    }

    public void updatePassword(Integer id, ChangePasswordRequest request) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        String currentPassword = request.getCurrentPassword();
        String newPassword = request.getNewPassword();

        if (!passwordEncoder.matches(currentPassword, existingUser.getPassword())) {
            throw new IncorrectCurrentPasswordException();
        }

        if (passwordEncoder.matches(newPassword, existingUser.getPassword())) {
            throw new PasswordNotChangedException();
        }

        existingUser.setPassword(passwordEncoder.encode(newPassword));
        existingUser.setPasswordUpdatedAt(LocalDateTime.now());
        userRepository.save(existingUser);
    }

    public void updatePhoneNumber(Integer id, ChangePhoneNumberRequest request) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        CountryCode countryCode = request.getCountryCode();
        String newPhoneNumber = request.getNewPhoneNumber();

        if (existingUser.getPhoneNumber().equals(newPhoneNumber)) {
            throw new PhoneNumberNotChangedException();
        }

        if(userRepository.findByPhoneNumber(newPhoneNumber).isPresent()) {
            throw new PhoneNumberAlreadyExistsException(newPhoneNumber);
        }

        if (!countryCode.equals(existingUser.getCountryCode())) {
            existingUser.setCountryCode(countryCode);
        }
        existingUser.setPhoneNumber(newPhoneNumber);
        existingUser.setPhoneVerified(false);
        existingUser.setUpdatedAt(LocalDateTime.now());
        userRepository.save(existingUser);
    }

    public void updateName(Integer id, ChangeNameRequest request) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        boolean isUpdated = false;

        if (request.getFirstName() != null && !request.getFirstName().equals(existingUser.getFirstName())) {
            existingUser.setFirstName(request.getFirstName());
            isUpdated = true;
        }

        if (request.getLastName() != null && !request.getLastName().equals(existingUser.getLastName())) {
            existingUser.setLastName(request.getLastName());
            isUpdated = true;
        }

        if (!isUpdated) {
            throw new NameNotChangedException();
        }

        existingUser.setUpdatedAt(LocalDateTime.now());
        userRepository.save(existingUser);
    }

    private void checkIfEmailOrPhoneExists(UserRegistrationRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        if (userRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
            throw new PhoneNumberAlreadyExistsException(request.getPhoneNumber());
        }
    }
}
