package ua.lastbite.userservice.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ua.lastbite.userservice.dto.user.ChangePhoneNumberRequestDto;
import ua.lastbite.userservice.model.CountryCode;

import java.util.stream.Stream;

class PhoneNumberValidatorTest {

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

    @ParameterizedTest
    @MethodSource("invalidPhoneNumberProvider")
    void shouldReturnFalseWheInvalidPhoneNumber(CountryCode countryCode, String phoneNumber) {
        ChangePhoneNumberRequestDto request = new ChangePhoneNumberRequestDto(countryCode, phoneNumber);
        assertFalse(changePhoneNumberValidator.isValidPhoneNumber(request.getCountryCode(), request.getNewPhoneNumber()));
    }

    static Stream<Arguments> invalidPhoneNumberProvider() {
        return Stream.of(
                Arguments.of(CountryCode.UA, "12345"),
                Arguments.of(CountryCode.UA, "123456789012345"),
                Arguments.of(CountryCode.UA, "abc123xyz"),
                Arguments.of(CountryCode.UA, null),
                Arguments.of(null, "987654321")
        );
    }
}