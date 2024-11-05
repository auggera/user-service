package ua.lastbite.userservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.lastbite.userservice.dto.email.UserEmailResponseDto;
import ua.lastbite.userservice.exception.user.UserNotFoundException;
import ua.lastbite.userservice.model.User;
import ua.lastbite.userservice.repository.UserRepository;

@Service
public class UserEmailService {

    private final UserRepository userRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(UserEmailService.class);

    @Autowired
    public UserEmailService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEmailResponseDto getUserEmailInfo(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return new UserEmailResponseDto(user.getEmail(), user.isEmailVerified());
    }

    public void verifyEmail(Integer userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.setEmailVerified(true);
        userRepository.save(user);

        LOGGER.info("Email for user ID {} has been verified", userId);
    }
}
