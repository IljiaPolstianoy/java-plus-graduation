package ru.practicum;

import ru.practicum.user.dto.UserDto;
import ru.practicum.exception.UserAlreadyExistsException;
import ru.practicum.user.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    UserDto createUser(UserDto userDto) throws UserAlreadyExistsException;

    void deleteUserById(Long userId);

    List<UserDto> findAllUsers(Integer from, Integer size, List<Long> ids);

    Optional<User> findById(Long userId);
}
