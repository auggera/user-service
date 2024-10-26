package ua.lastbite.userservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ua.lastbite.userservice.dto.token.TokenValidationRequest;
import ua.lastbite.userservice.dto.token.TokenValidationResponse;
import ua.lastbite.userservice.exception.token.TokenAlreadyUsedException;
import ua.lastbite.userservice.exception.token.TokenExpiredException;
import ua.lastbite.userservice.exception.token.TokenNotFoundException;
import ua.lastbite.userservice.exception.global.ServiceUnavailableException;

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

        try {
            return restTemplate.postForObject(urlRequest, request, TokenValidationResponse.class);
        } catch (HttpClientErrorException.NotFound e) {
            LOGGER.error("Token not found for request: {}", request.getTokenValue());
            throw new TokenNotFoundException("Token not found: " + request.getTokenValue());
        } catch (HttpClientErrorException.Gone e) {
            LOGGER.error("Token expired for request: {}", request.getTokenValue());
            throw new TokenExpiredException("Token expired: " + request.getTokenValue());
        } catch (HttpClientErrorException.Conflict e) {
            LOGGER.error("Token already used for request: {}", request.getTokenValue());
            throw new TokenAlreadyUsedException("Token already used: " + request.getTokenValue());
        } catch (HttpServerErrorException e) {
            LOGGER.error("Service unavailable for token validation request: {}", request.getTokenValue(), e);
            throw new ServiceUnavailableException("Token service is currently unavailable");
        } catch (RestClientException e) {
            LOGGER.error("Unexpected error during token validation request: {}", request.getTokenValue(), e);
            throw new ServiceUnavailableException("Unexpected error during token validation");
        }
    }
}
