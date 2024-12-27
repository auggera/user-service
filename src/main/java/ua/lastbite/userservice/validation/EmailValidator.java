package ua.lastbite.userservice.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EmailAddressValidator implements ConstraintValidator<ValidEmail, String> {

private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$";

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.trim().isEmpty()) {
            return true; // Let @NotBlank handle the case
        }

        return email.matches(EMAIL_REGEX);
    }
}
