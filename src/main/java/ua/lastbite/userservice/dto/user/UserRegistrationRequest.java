package ua.lastbite.userservice.dto.user;

import jakarta.validation.constraints.*;
import lombok.*;
import ua.lastbite.userservice.model.CountryCode;
import ua.lastbite.userservice.model.UserRole;
import ua.lastbite.userservice.validation.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@EqualsAndHashCode
@ValidPhoneNumber
public class UserRegistrationRequest {

    @NotBlank(message = "First name cannot be empty")
    @ValidName
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    private String firstName;

    @NotBlank(message = "Last name cannot be empty")
    @ValidName
    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    private String lastName;

    @NotBlank(message = "Email cannot be empty")
    @ValidEmail
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @ValidPassword
    private String password;

    @NotNull(message = "Country code is required")
    private CountryCode countryCode;

    @NotBlank(message = "Phone number cannot be empty")
    private String phoneNumber;

    @NotNull(message = "Role is required")
    private UserRole role;
}
