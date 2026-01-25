package ru.practicum.stats;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientRestStatConfig {

    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public ClientRestStat clientRestStat(@LoadBalanced RestClient.Builder restClientBuilder) {
        return new ClientRestStatImpl(restClientBuilder);
    }
}