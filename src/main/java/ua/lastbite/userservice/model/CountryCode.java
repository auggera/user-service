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
}