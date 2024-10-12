package ua.lastbite.userservice.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ua.lastbite.userservice.model.CountryCode;
import ua.lastbite.userservice.validation.ValidPhoneNumber;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ValidPhoneNumber
@EqualsAndHashCode
public class ChangePhoneNumberRequest {

    @NotNull(message = "Country code is required")
    private CountryCode countryCode;

    @NotBlank(message = "Phone number cannot be empty")
    private String newPhoneNumber;
}

