package ua.lastbite.userservice.dto.email;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class EmailInfoResponseDto {

    private String email;
    private boolean verified;
}
