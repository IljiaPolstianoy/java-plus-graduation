package ru.practicum.user.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.practicum.user.User;
import ru.practicum.user.UserService;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/internal/user")
public class InternalUserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    public Optional<User> findById(@PathVariable @Positive final Long userId) {
        return userService.findById(userId);
    }
}
