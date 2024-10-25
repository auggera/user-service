package ua.lastbite.userservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ua.lastbite.userservice.dto.token.TokenValidationRequest;
import ua.lastbite.userservice.dto.token.TokenValidationResponse;
import ua.lastbite.userservice.exception.token.TokenValidationException;

import java.util.Optional;

@Service
public class TokenServiceClient {

    private final RestTemplate restTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenServiceClient.class);

    @Value("${token-service.url}")
    private String tokenServiceUrl;

    @Autowired
    public TokenServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public TokenValidationResponse verifyToken(TokenValidationRequest request) {
        LOGGER.info("Validating token");
        String urlRequest = tokenServiceUrl + "/api/tokens/validate";

        return Optional.ofNullable(restTemplate.postForObject(urlRequest, request, TokenValidationResponse.class))
                .orElseThrow(() -> {
                    LOGGER.error("Token validation failed for request: {}", request);
                    return new TokenValidationException("Token validation failed");
                });
    }
}
