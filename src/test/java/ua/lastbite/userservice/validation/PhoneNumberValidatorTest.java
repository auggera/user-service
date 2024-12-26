package ua.lastbite.userservice.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import ua.lastbite.userservice.dto.user.ChangePhoneNumberRequestDto;
import ua.lastbite.userservice.model.CountryCode;

public class PhoneNumberValidatorTest {

    private ChangePhoneNumberValidator changePhoneNumberValidator;

    @BeforeEach
    void setup() {
        changePhoneNumberValidator = new ChangePhoneNumberValidator();
    }

    @Test
    void isValid() {
        ChangePhoneNumberRequestDto request = new ChangePhoneNumberRequestDto(CountryCode.UA, "123456789");
        assertTrue(changePhoneNumberValidator.isValidPhoneNumber(request.getCountryCode(), request.getNewPhoneNumber()));
    }

    @Test
    void testPhoneNumberTooShort() {
        ChangePhoneNumberRequestDto request = new ChangePhoneNumberRequestDto(CountryCode.UA, "12345");
        assertFalse(changePhoneNumberValidator.isValidPhoneNumber(request.getCountryCode(), request.getNewPhoneNumber()));
    }

    @Test
    void testPhoneNumberTooLong() {
        ChangePhoneNumberRequestDto request = new ChangePhoneNumberRequestDto(CountryCode.UA, "123456789012345");
        assertFalse(changePhoneNumberValidator.isValidPhoneNumber(request.getCountryCode(), request.getNewPhoneNumber()));
    }

    @Test
    void testInvalidPhoneNumberFormat() {
        ChangePhoneNumberRequestDto request = new ChangePhoneNumberRequestDto(CountryCode.UA, "abc123xyz");
        assertFalse(changePhoneNumberValidator.isValidPhoneNumber(request.getCountryCode(), request.getNewPhoneNumber()));
    }

    @Test
    void testNullPhoneNumber() {
        ChangePhoneNumberRequestDto request = new ChangePhoneNumberRequestDto(CountryCode.UA, null);
        assertFalse(changePhoneNumberValidator.isValidPhoneNumber(request.getCountryCode(), request.getNewPhoneNumber()));
    }

    @Test
    void testNullCountryCode() {
        ChangePhoneNumberRequestDto request = new ChangePhoneNumberRequestDto(null, "987654321");
        assertFalse(changePhoneNumberValidator.isValidPhoneNumber(request.getCountryCode(), request.getNewPhoneNumber()));
    }
}