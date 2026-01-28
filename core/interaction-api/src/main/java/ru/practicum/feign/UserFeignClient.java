package ru.practicum.feign;

import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.user.User;

import java.util.Optional;

@FeignClient(name = "user-service", path = "/internal/user")
public interface UserFeignClient {

    @GetMapping("/{userId}")
    Optional<User> findById(@PathVariable @Positive final Long userId);
}
