package ua.lastbite.userservice.service;

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

    @Value("${token-service.url}")
    private String tokenServiceUrl;

    @Autowired
    public TokenServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean validateToken(TokenValidationRequest request) {
        String urlRequest = tokenServiceUrl + "/api/tokens/validate";

        TokenValidationResponse response = restTemplate.postForObject(urlRequest, request, TokenValidationResponse.class);

        if (response == null) {
            throw new TokenValidationException("Something went wrong.");
        }

        return response.isValid();
    }
}
