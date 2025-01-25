package ua.lastbite.userservice.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class CustomEmailValidatorTest {

    private CustomEmailValidator emailValidator;
    private ConstraintValidatorContext context;

    @BeforeEach
    public void setUp() {
        emailValidator = new CustomEmailValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @ParameterizedTest
    @MethodSource("validEmailProvider")
    void validEmailAddress(String email) {
        assertTrue(emailValidator.isValid(email, context));
    }

    static Stream<Arguments> validEmailProvider() {
        return Stream.of(
                Arguments.of("valid@email.com"),
                Arguments.of(""), // handles @NotBlank
                Arguments.of( (Object) null) // handles @NotBlank
        );
    }

    @ParameterizedTest
    @MethodSource("invalidEmailProvider")
    void shouldReturnFalseWhenInvalidEmail(String invalidEmail) {
        assertFalse(emailValidator.isValid(invalidEmail, context), "Email " + invalidEmail + " should be invalid");
    }

    static Stream<Arguments> invalidEmailProvider() {
        return Stream.of(
                Arguments.of("example.com"),
                Arguments.of("email@.com"),
                Arguments.of("email@com"),
                Arguments.of("test@email@example.com"),
                Arguments.of("email@example,com"),
                Arguments.of("email@example.c"),
                Arguments.of("test email@example.com"),
                Arguments.of("test@example.com."),
                Arguments.of("email@example..com")
        );
    }
}
