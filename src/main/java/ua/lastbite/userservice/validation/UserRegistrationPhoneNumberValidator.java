package ua.lastbite.userservice.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ua.lastbite.userservice.dto.UserRegistrationRequest;

public class UserRegistrationPhoneNumberValidator
        extends AbstractPhoneNumberValidator
        implements ConstraintValidator<ValidPhoneNumber, UserRegistrationRequest> {

    @Override
    public boolean isValid(UserRegistrationRequest request, ConstraintValidatorContext context) {
        if (request.getPhoneNumber() == null) {
            return true; // Let @NotBlank handle the case
        }

        context.disableDefaultConstraintViolation();

        if (!isValidPhoneNumber(request.getCountryCode(), request.getPhoneNumber())) {
            context.buildConstraintViolationWithTemplate("Invalid phone number format")
                    .addPropertyNode("phoneNumber")
                    .addConstraintViolation();

            return false;
        }
        return true;
    }
}

