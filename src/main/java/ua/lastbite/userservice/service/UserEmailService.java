package ua.lastbite.userservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.lastbite.userservice.dto.email.UserEmailResponseDto;
import ua.lastbite.userservice.dto.token.TokenValidationRequest;
import ua.lastbite.userservice.dto.token.TokenValidationResponse;
import ua.lastbite.userservice.exception.token.InvalidTokenException;
import ua.lastbite.userservice.exception.token.TokenValidationException;
import ua.lastbite.userservice.exception.user.UserNotFoundException;
import ua.lastbite.userservice.model.User;
import ua.lastbite.userservice.repository.UserRepository;

@Service
public class UserEmailService {

    private final UserRepository userRepository;
    private final TokenServiceClient tokenServiceClient;
    private static final Logger LOGGER = LoggerFactory.getLogger(UserEmailService.class);

    @Autowired
    public UserEmailService(UserRepository userRepository, TokenServiceClient tokenServiceClient) {
        this.userRepository = userRepository;
        this.tokenServiceClient = tokenServiceClient;
    }

    public UserEmailResponseDto getUserEmailInfo(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return new UserEmailResponseDto(user.getEmail(), user.isEmailVerified());
    }

    public void verifyEmail(TokenValidationRequest request) {
        LOGGER.info("Processing token validation request");

        TokenValidationResponse response = tokenServiceClient.verifyToken(request);

        LOGGER.info("Received response :{}", response);

        if (!response.isValid()) {
            throw new InvalidTokenException("Token is invalid or expired");
        }

        User user = userRepository.findById(response.getUserId())
                .orElseThrow(() -> new UserNotFoundException(response.getUserId()));

        user.setEmailVerified(true);
        userRepository.save(user);

        LOGGER.info("Email for user ID {} has been verified", response.getUserId());
    }
}
