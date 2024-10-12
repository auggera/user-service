package ua.lastbite.userservice.dto.token;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class TokenValidationResponse {

    private boolean valid;
    private Integer userId;
}