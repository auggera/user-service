package ua.lastbite.userservice.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import ua.lastbite.userservice.dto.user.ChangePhoneNumberRequest;
import ua.lastbite.userservice.model.CountryCode;

public class PhoneNumberValidatorTest {

    private ChangePhoneNumberValidator changePhoneNumberValidator;

    @BeforeEach
    void setup() {
        changePhoneNumberValidator = new ChangePhoneNumberValidator();
    }

    @Test
    void isValid() {
        ChangePhoneNumberRequest request = new ChangePhoneNumberRequest(CountryCode.UA, "123456789");
        assertTrue(changePhoneNumberValidator.isValidPhoneNumber(request.getCountryCode(), request.getNewPhoneNumber()));
    }

    @Test
    void testPhoneNumberTooShort() {
        ChangePhoneNumberRequest request = new ChangePhoneNumberRequest(CountryCode.UA, "12345");
        assertFalse(changePhoneNumberValidator.isValidPhoneNumber(request.getCountryCode(), request.getNewPhoneNumber()));
    }

    @Test
    void testPhoneNumberTooLong() {
        ChangePhoneNumberRequest request = new ChangePhoneNumberRequest(CountryCode.UA, "123456789012345");
        assertFalse(changePhoneNumberValidator.isValidPhoneNumber(request.getCountryCode(), request.getNewPhoneNumber()));
    }

    @Test
    void testInvalidPhoneNumberFormat() {
        ChangePhoneNumberRequest request = new ChangePhoneNumberRequest(CountryCode.UA, "abc123xyz");
        assertFalse(changePhoneNumberValidator.isValidPhoneNumber(request.getCountryCode(), request.getNewPhoneNumber()));
    }

    @Test
    void testNullPhoneNumber() {
        ChangePhoneNumberRequest request = new ChangePhoneNumberRequest(CountryCode.UA, null);
        assertFalse(changePhoneNumberValidator.isValidPhoneNumber(request.getCountryCode(), request.getNewPhoneNumber()));
    }

    @Test
    void testNullCountryCode() {
        ChangePhoneNumberRequest request = new ChangePhoneNumberRequest(null, "987654321");
        assertFalse(changePhoneNumberValidator.isValidPhoneNumber(request.getCountryCode(), request.getNewPhoneNumber()));
    }
}