package ru.practicum.request;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import ru.practicum.exception.handler.ErrorHandlerConfig;

@SpringBootApplication
@EnableFeignClients(basePackages = "ru.practicum.feign")
@EntityScan(basePackages = "ru.practicum")
@Import(ErrorHandlerConfig.class)
public class RequestApplication {
    public static void main(String[] args) {
        SpringApplication.run(RequestApplication.class, args);
    }
}
