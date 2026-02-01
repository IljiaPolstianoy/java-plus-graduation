// В модуле interaction-api
package ru.practicum.exception.handler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ErrorHandlerConfig {

    @Bean
    public ErrorHandler errorHandler() {
        return new ErrorHandler();
    }
}