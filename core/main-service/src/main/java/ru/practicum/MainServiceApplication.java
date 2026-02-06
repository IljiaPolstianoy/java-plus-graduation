package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.practicum.exception.handler.ErrorHandlerConfig;


@SpringBootApplication
@EnableJpaRepositories(basePackages = "ru.practicum.mainservice")
@EntityScan(basePackages = "ru.practicum")
@ComponentScan(basePackages = {
        "ru.practicum.mainservice",
        "ru.practicum.stats",
        "ru.practicum.exception",
        "ru.practicum.event"
})
@EnableFeignClients(basePackages = "ru.practicum.feign")
@Import(ErrorHandlerConfig.class)
public class MainServiceApplication {

    public static void main(final String[] args) {
        SpringApplication.run(MainServiceApplication.class, args);
    }

}
