package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import ru.practicum.exception.handler.ErrorHandlerConfig;

@SpringBootApplication
@Import(ErrorHandlerConfig.class)
@EnableFeignClients(basePackages = "ru.practicum.feign")
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}
