package ua.lastbite.userservice.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ua.lastbite.userservice.dto.ChangePhoneNumberRequest;

public class ChangePhoneNumberValidator
        extends AbstractPhoneNumberValidator
        implements ConstraintValidator<ValidPhoneNumber, ChangePhoneNumberRequest> {

    @Override
    public boolean isValid(ChangePhoneNumberRequest request, ConstraintValidatorContext context) {
        if (request.getNewPhoneNumber() == null) {
            return true; // Let @NotBlank handle the case
        }

        context.disableDefaultConstraintViolation();

        if (!isValidPhoneNumber(request.getCountryCode(), request.getNewPhoneNumber())) {
            context.buildConstraintViolationWithTemplate("Invalid phone number format")
                    .addPropertyNode("newPhoneNumber")
                    .addConstraintViolation();

            return false;
        }

        return true;
    }
}
