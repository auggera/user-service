package ua.lastbite.userservice.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ua.lastbite.userservice.dto.user.UserRegistrationRequest;
import ua.lastbite.userservice.model.CountryCode;
import ua.lastbite.userservice.model.User;
import ua.lastbite.userservice.model.UserRole;

import static org.junit.jupiter.api.Assertions.*;

public class UserRegistrationMapperTest {

    private UserRegistrationMapper userRegistrationMapper;

    @BeforeEach
    void setUp() {
        userRegistrationMapper = Mappers.getMapper(UserRegistrationMapper.class);
    }

    @Test
    void testUserRegistrationMapper() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("email@example.com");
        request.setPassword("password123");
        request.setCountryCode(CountryCode.UA);
        request.setPhoneNumber("1234567890");
        request.setRole(UserRole.CUSTOMER);

        User user = userRegistrationMapper.toUser(request);

        assertNotNull(user);
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("email@example.com", user.getEmail());
        assertEquals(CountryCode.UA, user.getCountryCode());
        assertEquals("1234567890", user.getPhoneNumber());
        assertEquals(UserRole.CUSTOMER, user.getRole());
    }
}
