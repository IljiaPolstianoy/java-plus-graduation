package ru.practicum.user;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.config.CommonMapperConfiguration;
import ru.practicum.user.dto.UserDto;

@Mapper(config = CommonMapperConfiguration.class)
public interface UserMapper {

    User toEntity(UserDto userDto);

    UserDto toUserDto(User entity);

    @Mapping(target = "email", ignore = true)
    UserDto toUserDtoShort(User entity);
}
