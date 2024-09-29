package ua.lastbite.userservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CountryCode {
    UA("+380", new int[] {9, 9}, "Ukraine");

    private final String code;
    private final int[] phoneLengthRange;
    private final String countryName;

    public static CountryCode getByCode(String code) {
        for (CountryCode countryCode : values()) {
            if (countryCode.getCode().equals(code)
                    || countryCode.name().equalsIgnoreCase(code.trim())) {
                return countryCode;
            }
        }
        return null;
    }
}