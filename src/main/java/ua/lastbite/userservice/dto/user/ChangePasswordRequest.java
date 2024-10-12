package ua.lastbite.userservice.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import ua.lastbite.userservice.validation.ValidPassword;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@EqualsAndHashCode
public class ChangePasswordRequest {

    @NotBlank(message = "Current password cannot be empty")
    private String currentPassword;

    @NotBlank(message = "New password cannot be empty")
    @ValidPassword
    private String newPassword;
}
