package ua.lastbite.userservice.validation;

import org.apache.commons.validator.routines.EmailValidator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CustomEmailValidator implements ConstraintValidator<ValidEmail, String> {

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.trim().isEmpty()) {
            return true; // Let @NotBlank handle the case
        }

        return EmailValidator.getInstance().isValid(email);
    }
}
