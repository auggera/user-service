package ua.lastbite.userservice.dto.email;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class UserEmailInfo {

    private String email;
    private boolean verified;
}
