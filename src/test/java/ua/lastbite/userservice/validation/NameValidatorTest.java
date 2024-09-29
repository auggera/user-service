package ua.lastbite.userservice.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class NameValidatorTest {

    private NameValidator nameValidator = new NameValidator();
    private ConstraintValidatorContext context;

    @BeforeEach
    public void setUp() {
        nameValidator = new NameValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    void testValidName() {
        assertTrue(nameValidator.isValid("John Doe", context));
        assertTrue(nameValidator.isValid("O'Connor", context));
        assertTrue(nameValidator.isValid("Anne-Marie", context));
    }

    @Test
    void testInvalidName() {
        assertFalse(nameValidator.isValid("John123", context));
        assertFalse(nameValidator.isValid("Doe@", context));
        assertFalse(nameValidator.isValid(".", context));
    }
}
