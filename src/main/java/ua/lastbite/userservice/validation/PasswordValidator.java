package ua.lastbite.userservice.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private static final Pattern LETTER = Pattern.compile(".*[a-zA-Z].*");
    private static final Pattern DIGIT = Pattern.compile(".*[0-9].*");

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {

        if (password == null) {
            return true; // Let @NotBlank handle the case
        }

        return password.length() >= 8
                && LETTER.matcher(password).matches() && DIGIT.matcher(password).matches();
    }
}
