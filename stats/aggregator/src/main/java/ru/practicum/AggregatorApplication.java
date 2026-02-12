package ru.practicum;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class AggregatorApplication {
    public static void main(String[] args) {
        final ConfigurableApplicationContext context = SpringApplication.run(AggregatorApplication.class, args);

        final Aggregator aggregator = context.getBean(Aggregator.class);
        aggregator.start();
    }
}