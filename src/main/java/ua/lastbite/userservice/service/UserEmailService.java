package ua.lastbite.userservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.lastbite.userservice.dto.email.UserEmailResponseDto;
import ua.lastbite.userservice.exception.user.UserNotFoundException;
import ua.lastbite.userservice.model.User;
import ua.lastbite.userservice.repository.UserRepository;

@Service
public class UserEmailService {

    private final UserRepository userRepository;

    @Autowired
    public UserEmailService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEmailResponseDto getUserEmailInfo(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return new UserEmailResponseDto(user.getEmail(), user.isEmailVerified());
    }
}
