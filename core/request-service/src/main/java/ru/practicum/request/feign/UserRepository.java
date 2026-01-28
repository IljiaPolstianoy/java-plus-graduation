package ru.practicum.request.feign;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.feign.UserFeignClient;
import ru.practicum.user.User;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserRepository {

    private final UserFeignClient userFeignClient;

    public Optional<User> findById(final Long userId) {
        return userFeignClient.findById(userId);
    }
}
