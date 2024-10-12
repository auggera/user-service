package ua.lastbite.userservice.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ua.lastbite.userservice.dto.user.UserResponseDto;
import ua.lastbite.userservice.model.User;
import ua.lastbite.userservice.model.UserRole;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserResponseMapperTest {

    private UserResponseMapper userResponseMapper ;

    @BeforeEach
    void setUp() {
        userResponseMapper = Mappers.getMapper(UserResponseMapper.class);
    }

    @Test
    void testToUserResponseDto() {
        User user = new User();
        user.setId(1);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@doe.com");
        user.setRole(UserRole.CUSTOMER);

        UserResponseDto responseDto = userResponseMapper.toUserResponseDto(user);

        assertNotNull(responseDto);
        assertEquals(1, responseDto.getId());
        assertEquals("John", responseDto.getFirstName());
        assertEquals("Doe", responseDto.getLastName());
        assertEquals("john@doe.com", responseDto.getEmail());
        assertEquals(UserRole.CUSTOMER, responseDto.getRole());
    }

    @Test
    void testToUserResponseDtoPage() {
        User user1 = new User();
        user1.setId(1);
        user1.setFirstName("John");
        user1.setLastName("Doe");
        user1.setEmail("john@doe.com");
        user1.setRole(UserRole.CUSTOMER);

        User user2 = new User();
        user2.setId(2);
        user2.setFirstName("Jane");
        user2.setLastName("Doe");
        user2.setEmail("jane@doe.com");
        user2.setRole(UserRole.BUSINESS_OWNER);

        Page<User> usersPage = new PageImpl<>(List.of(user1, user2), PageRequest.of(0, 2), 2);
        Page<UserResponseDto> responseDtoPage = userResponseMapper.toUserResponseDtoPage(usersPage);

        assertNotNull(responseDtoPage);
        assertEquals(2, responseDtoPage.getTotalElements());

        UserResponseDto responseDto1 = responseDtoPage.getContent().get(0);
        UserResponseDto responseDto2 = responseDtoPage.getContent().get(1);

        assertEquals(1, responseDto1.getId());
        assertEquals("John", responseDto1.getFirstName());
        assertEquals("Doe", responseDto1.getLastName());
        assertEquals("john@doe.com", responseDto1.getEmail());
        assertEquals(UserRole.CUSTOMER, responseDto1.getRole());

        assertEquals(2, responseDto2.getId());
        assertEquals("Jane", responseDto2.getFirstName());
        assertEquals("Doe", responseDto2.getLastName());
        assertEquals("jane@doe.com", responseDto2.getEmail());
        assertEquals(UserRole.BUSINESS_OWNER, responseDto2.getRole());
    }

}
