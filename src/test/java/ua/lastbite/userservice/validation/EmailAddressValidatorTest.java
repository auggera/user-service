package ua.lastbite.userservice.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class EmailAddressValidatorTest {

    private EmailAddressValidator emailAddressValidator;
    private ConstraintValidatorContext context;

    @BeforeEach
    public void setUp() {
        emailAddressValidator = new EmailAddressValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    void validEmailAddress() {
        assertTrue(emailAddressValidator.isValid("email@example.com", context));
    }

    @Test
    void invalidEmailAddress() {
        assertFalse(emailAddressValidator.isValid("example.com", context));
        assertFalse(emailAddressValidator.isValid("@example.com", context));
        assertFalse(emailAddressValidator.isValid("email@.com", context));
        assertFalse(emailAddressValidator.isValid("email@com", context));
        assertFalse(emailAddressValidator.isValid("test@email@example.com", context));
        assertFalse(emailAddressValidator.isValid("email@example,com", context));
        assertFalse(emailAddressValidator.isValid("email@example.c", context));
        assertFalse(emailAddressValidator.isValid("test email@example.com", context));
        assertFalse(emailAddressValidator.isValid("test@example.com.", context));
        assertFalse(emailAddressValidator.isValid("email@example..com", context));
    }
}
