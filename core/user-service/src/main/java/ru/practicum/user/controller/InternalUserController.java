package ru.practicum.user.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.exception.UserNotFoundException;
import ru.practicum.user.User;
import ru.practicum.user.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/user")
public class InternalUserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    public User findById(@PathVariable @Positive final Long userId) {
        return userService.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(String.format("User with id=%d was not found", userId)));
    }
}
