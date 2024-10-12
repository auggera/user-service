package ua.lastbite.userservice.dto.email;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class UserEmailResponseDto {

    private String email;
    private boolean emailVerified;
}
