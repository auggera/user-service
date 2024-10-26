package ua.lastbite.userservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ExpectedCount;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@ActiveProfiles("test")
@SpringBootTest
public class TokenServiceClientTest {

    @Autowired
    private TokenServiceClient tokenServiceClient;

    @MockBean
    private RestTemplate restTemplate;

    @Value("${token-service.url}")
    private String tokenServiceUrl;

    private MockRestServiceServer mockServer;
    private TokenValidationRequest tokenValidationRequest;
    private TokenValidationResponse expectedResponse;
    private String urlRequest;



    @BeforeEach
    void setUp() {
        tokenValidationRequest = new TokenValidationRequest("testToken123");

        expectedResponse = new TokenValidationResponse(true, 1);

        urlRequest = tokenServiceUrl + "/api/tokens/validate";
        System.out.println("URL Request: " + urlRequest);

        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
    }

    @Test
    void verifyTokenSuccessfully() {

        Mockito.when(restTemplate.postForObject(urlRequest, tokenValidationRequest, TokenValidationResponse.class)).thenReturn(expectedResponse);

        mockServer.expect(ExpectedCount.once(), requestTo(urlRequest))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"valid\":true, \"userId\":1}", MediaType.APPLICATION_JSON));

        TokenValidationResponse actualResponse = tokenServiceClient.verifyToken(tokenValidationRequest);

        assertNotNull(actualResponse, "Response should not be null");
        assertEquals(expectedResponse, actualResponse);
        assertTrue(actualResponse.isValid());
    }

    @Test
    void verifyTokenNotFound() {

        Mockito.when(restTemplate.postForObject(urlRequest, tokenValidationRequest, TokenValidationResponse.class))
                .thenThrow(HttpClientErrorException.NotFound.create(HttpStatus.NOT_FOUND, "Exception occurred", null, null, null));

        TokenNotFoundException exception = assertThrows(TokenNotFoundException.class, () -> tokenServiceClient.verifyToken(tokenValidationRequest));

        assertEquals(exception.getMessage(), "Token not found: " + tokenValidationRequest.getTokenValue());
    }

    @Test
    void verifyTokenExpired() {

        Mockito.when(restTemplate.postForObject(urlRequest, tokenValidationRequest, TokenValidationResponse.class))
                .thenThrow(HttpClientErrorException.Gone.create(HttpStatus.GONE, "Exception occurred", null, null, null));

        TokenExpiredException exception = assertThrows(TokenExpiredException.class, () -> tokenServiceClient.verifyToken(tokenValidationRequest));

        assertEquals(exception.getMessage(), "Token expired: " + tokenValidationRequest.getTokenValue());
    }

    @Test
    void verifyTokenIsAlreadyUsed() {

        Mockito.when(restTemplate.postForObject(urlRequest, tokenValidationRequest, TokenValidationResponse.class))
                .thenThrow(HttpClientErrorException.Conflict.create(HttpStatus.CONFLICT, "Exception occurred", null, null, null));

        TokenAlreadyUsedException exception = assertThrows(TokenAlreadyUsedException.class, () -> tokenServiceClient.verifyToken(tokenValidationRequest));

        assertEquals(exception.getMessage(), "Token already used: " + tokenValidationRequest.getTokenValue());
    }

    @Test
    void verifyTokenServiceUnavailable() {

        Mockito.when(restTemplate.postForObject(urlRequest, tokenValidationRequest, TokenValidationResponse.class))
                .thenThrow(HttpServerErrorException.InternalServerError.class);

        ServiceUnavailableException exception = assertThrows(ServiceUnavailableException.class, () -> tokenServiceClient.verifyToken(tokenValidationRequest));

        assertEquals(exception.getMessage(), "Token service is currently unavailable");
    }

    @Test
    void verifyTokenRestClientException() {

        Mockito.when(restTemplate.postForObject(urlRequest, tokenValidationRequest, TokenValidationResponse.class))
                .thenThrow(RestClientException.class);

        ServiceUnavailableException exception = assertThrows(ServiceUnavailableException.class, () -> tokenServiceClient.verifyToken(tokenValidationRequest));

        assertEquals(exception.getMessage(), "Unexpected error during token validation");
    }
}
