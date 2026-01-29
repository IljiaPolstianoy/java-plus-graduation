package ru.practicum.user.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.exception.UserAlreadyExistsException;
import ru.practicum.user.UserService;
import ru.practicum.user.dto.UserDto;
import ru.practicum.validation.ValidationGroups;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Validated
public class UserControllerAdmin {

    private final UserService userService;

    @GetMapping
    public List<UserDto> findAll(
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size,
            @RequestParam(required = false) List<Long> ids
    ) {
        return userService.findAllUsers(from, size, ids);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto save(@RequestBody @Validated({ValidationGroups.Create.class, Default.class})  UserDto userDto) throws UserAlreadyExistsException {
        return userService.createUser(userDto);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long userId) {
        userService.deleteUserById(userId);
    }
}

