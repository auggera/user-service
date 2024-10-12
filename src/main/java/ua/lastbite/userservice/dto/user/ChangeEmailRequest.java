package ua.lastbite.userservice.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import ua.lastbite.userservice.validation.ValidEmail;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class ChangeEmailRequest {

    @NotBlank(message = "New email cannot be empty")
    @ValidEmail
    private String newEmail;
}
