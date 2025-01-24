package ua.lastbite.userservice.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    @Test
    void validEmailAddress() {
        assertTrue(emailValidator.isValid("email@example.com", context));
    }

    @Test
    void invalidEmailAddress() {
        assertFalse(emailValidator.isValid("example.com", context));
        assertFalse(emailValidator.isValid("@example.com", context));
        assertFalse(emailValidator.isValid("email@.com", context));
        assertFalse(emailValidator.isValid("email@com", context));
        assertFalse(emailValidator.isValid("test@email@example.com", context));
        assertFalse(emailValidator.isValid("email@example,com", context));
        assertFalse(emailValidator.isValid("email@example.c", context));
        assertFalse(emailValidator.isValid("test email@example.com", context));
        assertFalse(emailValidator.isValid("test@example.com.", context));
        assertFalse(emailValidator.isValid("email@example..com", context));
    }
}
