package ru.practicum.event;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableFeignClients(basePackages = "ru.practicum.feign")
@ComponentScan(basePackages = {
        "ru.practicum.stats",
        "ru.practicum.event"
})
public class EventApplication {
    public static void main(String[] args) {
        SpringApplication.run(EventApplication.class, args);
    }
}
