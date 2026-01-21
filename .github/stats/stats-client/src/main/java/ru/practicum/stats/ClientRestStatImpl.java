package ru.practicum.stats;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class ClientRestStatImpl implements ClientRestStat {

    private static final String STATS_SERVICE_ID = "STATS-SERVER";
    private final RestClient restClient;
    private final DiscoveryClient discoveryClient;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ClientRestStatImpl(RestClient restClient, DiscoveryClient discoveryClient) {
        this.restClient = restClient;
        this.discoveryClient = discoveryClient;
    }

    @Override
    @Retryable(
            retryFor = {ResourceAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 3000)
    )
    public Boolean addStat(EndpointHitDto dto) {
        try {
            URI serviceUri = buildServiceUri("/hit");
            return restClient.post()
                    .uri(serviceUri)
                    .body(dto)
                    .retrieve()
                    .body(Boolean.class);
        } catch (ResourceAccessException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Retryable(
            retryFor = {ResourceAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 3000)
    )
    public List<ViewStatsDto> getStat(LocalDateTime start,
                                      LocalDateTime end,
                                      List<String> uris,
                                      boolean unique) {
        try {
            String statsPath = buildStatsPath(start, end, uris, unique);
            URI serviceUri = buildServiceUri(statsPath);

            ResponseEntity<ViewStatsDto[]> responseEntity = restClient.get()
                    .uri(serviceUri)
                    .retrieve()
                    .toEntity(ViewStatsDto[].class);

            return responseEntity.getBody() != null ?
                    Arrays.asList(responseEntity.getBody()) : Collections.emptyList();
        } catch (ResourceAccessException e) {
            return Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * Строит полный URI к сервису статистики, используя DiscoveryClient.
     */
    private URI buildServiceUri(String path) {
        List<ServiceInstance> instances = discoveryClient.getInstances(STATS_SERVICE_ID);

        if (instances == null || instances.isEmpty()) {
            throw new IllegalStateException("Stats service not found: " + STATS_SERVICE_ID);
        }

        ServiceInstance instance = instances.get(0);
        return URI.create("http://" + instance.getHost() + ":" + instance.getPort() + path);
    }

    /**
     * Строит путь для запроса статистики с параметрами.
     */
    private String buildStatsPath(LocalDateTime start,
                                  LocalDateTime end,
                                  List<String> uris,
                                  boolean unique) {

        final String startStr = start.format(FORMATTER);
        final String endStr = end.format(FORMATTER);

        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/stats")
                .queryParam("start", startStr)
                .queryParam("end", endStr)
                .queryParam("unique", unique);

        if (uris != null && !uris.isEmpty()) {
            builder.queryParam("uris", String.join(",", uris));
        }

        return builder.build().encode().toUriString();
    }
}