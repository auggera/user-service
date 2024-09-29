package ua.lastbite.userservice.validation;

import ua.lastbite.userservice.model.CountryCode;

public abstract class AbstractPhoneNumberValidator {

    protected boolean isValidPhoneNumber(CountryCode countryCode, String phoneNumber) {
        if (countryCode == null || phoneNumber == null) {
            return false;
        }

        int[] lengthRange = countryCode.getPhoneLengthRange();
        return phoneNumber.length() >= lengthRange[0]
                && phoneNumber.length() <= lengthRange[1]
                && phoneNumber.matches("\\d+");
    }
}
