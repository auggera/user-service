package ua.lastbite.userservice.dto;

import lombok.*;
import ua.lastbite.userservice.model.UserRole;


@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@EqualsAndHashCode
public class UserResponseDto {

    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private UserRole role;
}
