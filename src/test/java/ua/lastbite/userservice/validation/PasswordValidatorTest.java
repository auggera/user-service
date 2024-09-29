package ua.lastbite.userservice.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class PasswordValidatorTest {

    private PasswordValidator passwordValidator;
    private ConstraintValidatorContext context;

    @BeforeEach
    public void setUp() {
        passwordValidator = new PasswordValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    void testValidPassword() {
        assertTrue(passwordValidator.isValid("password123", context));
    }

    @Test
    void testPasswordTooShort() {
        assertFalse(passwordValidator.isValid("pass12", context));
    }

    @Test
    void testPasswordNoLetters() {
        assertFalse(passwordValidator.isValid("12345678", context));
    }

    @Test
    void testPasswordNoDigits() {
        assertFalse(passwordValidator.isValid("password", context));
    }

    @Test
    void testPasswordEmptyString() {
        assertFalse(passwordValidator.isValid("", context));
    }
}
