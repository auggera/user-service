package ua.lastbite.userservice.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class NameValidatorTest {

    private NameValidator nameValidator = new NameValidator();
    private ConstraintValidatorContext context;

    @BeforeEach
    public void setUp() {
        nameValidator = new NameValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @ParameterizedTest
    @MethodSource("validNameProvider")
    void testValidName(String name) {
        assertTrue(nameValidator.isValid(name, context), "Name " + name + " should be valid");
    }

    static Stream<Arguments> validNameProvider() {
        return Stream.of(
                Arguments.of("John Doe"),
                Arguments.of("O'Connor"),
                Arguments.of("Anne-Marie"),
                Arguments.of(""), // handles @NotBlank
                Arguments.of( (Object) null) // handles @NotBlank
        );
    }

    @ParameterizedTest
    @MethodSource("invalidNameProvider")
    void testInvalidName(String name) {
        assertFalse(nameValidator.isValid(name, context), "Name " + name + " should be invalid");
    }

    static Stream<Arguments> invalidNameProvider() {
        return Stream.of(
                Arguments.of("John123"),
                Arguments.of("Doe@"),
                Arguments.of(".")
        );
    }
}
