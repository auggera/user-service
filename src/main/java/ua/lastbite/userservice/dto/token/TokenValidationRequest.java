package ua.lastbite.userservice.dto.token;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class TokenValidationRequest {

    @NotBlank(message = "Token cannot be empty")
    private String tokenValue;
}
