package ua.lastbite.userservice.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SizeValidator implements ConstraintValidator<ValidSize, String> {

    private int min;
    private int max;

    @Override
    public void initialize(ValidSize constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null) {
            return true; // we do not handle null
        }
        return value.length() >= min && value.length() <= max;
    }
}
