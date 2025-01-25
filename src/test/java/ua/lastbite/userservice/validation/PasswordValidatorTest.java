package ua.lastbite.userservice.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class PasswordValidatorTest {

    private PasswordValidator passwordValidator;
    private ConstraintValidatorContext context;

    @BeforeEach
    public void setUp() {
        passwordValidator = new PasswordValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @ParameterizedTest
    @MethodSource("validPasswordProvider")
    void testValidPassword(String password) {
        assertTrue(passwordValidator.isValid(password, context), "Password " + password + " should be valid");
    }

    static Stream<Arguments> validPasswordProvider() {
        return Stream.of(
                Arguments.of("password123"),
                Arguments.of(""), // handles @NotBlank
                Arguments.of( (Object) null) // handles @NotBlank
        );
    }

    @ParameterizedTest
    @MethodSource("invalidPasswordProvider")
    void testPasswordNoLetters(String password) {
        assertFalse(passwordValidator.isValid(password, context), "Password " + password + " should be invalid");
    }

    static Stream<Arguments> invalidPasswordProvider() {
        return Stream.of(
                Arguments.of("pass12"),
                Arguments.of("12345678"),
                Arguments.of("password")
        );
    }
}
