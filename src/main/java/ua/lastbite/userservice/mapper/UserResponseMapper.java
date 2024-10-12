package ua.lastbite.userservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import ua.lastbite.userservice.dto.user.UserResponseDto;
import ua.lastbite.userservice.model.User;

@Mapper(componentModel = "spring")
public interface UserResponseMapper  {

    UserResponseMapper  INSTANCE = Mappers.getMapper(UserResponseMapper .class);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "role", target = "role")
    UserResponseDto toUserResponseDto(User user);

    default Page<UserResponseDto> toUserResponseDtoPage(Page<User> usersPage) {
        return usersPage.map(this::toUserResponseDto);
    }
}
