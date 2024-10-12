package ua.lastbite.userservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ua.lastbite.userservice.dto.user.UserRegistrationRequest;
import ua.lastbite.userservice.model.User;

@Mapper(componentModel = "spring")
public interface UserRegistrationMapper {

    UserRegistrationMapper INSTANCE = Mappers.getMapper(UserRegistrationMapper.class);

    @Mapping(target = "password", ignore = true)
    User toUser(UserRegistrationRequest request);
}
