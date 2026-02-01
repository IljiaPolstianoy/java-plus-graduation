package ru.practicum.user;

import ru.practicum.exception.UserAlreadyExistsException;
import ru.practicum.user.dto.UserDto;

import java.util.List;
import java.util.Optional;

public interface UserService {
    UserDto createUser(UserDto userDto) throws UserAlreadyExistsException;

    void deleteUserById(Long userId);

    List<UserDto> findAllUsers(Integer from, Integer size, List<Long> ids);

    Optional<User> findById(Long userId);
}
