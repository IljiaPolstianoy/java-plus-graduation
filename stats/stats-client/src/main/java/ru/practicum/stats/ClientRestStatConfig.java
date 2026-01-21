package ru.practicum.stats;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientRestStatConfig {

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public RestClient statsRestClient(RestClient.Builder restClientBuilder) {
        return restClientBuilder
                .build();
    }

    @Bean
    public ClientRestStat clientRestStat(RestClient statsRestClient, DiscoveryClient discoveryClient) {
        return new ClientRestStatImpl(statsRestClient, discoveryClient);
    }
}