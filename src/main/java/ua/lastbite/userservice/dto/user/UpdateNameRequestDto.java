package ua.lastbite.userservice.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import ua.lastbite.userservice.validation.ValidName;
import ua.lastbite.userservice.validation.ValidSize;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@EqualsAndHashCode
public class UpdateNameRequestDto {

    @ValidName
    @ValidSize(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    private String firstName;

    @ValidName
    @ValidSize(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    private String lastName;
}
