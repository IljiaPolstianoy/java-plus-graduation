package ru.practicum.stats;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Component
public class ClientRestStatImpl implements ClientRestStat {

    private static final Logger logger = LoggerFactory.getLogger(ClientRestStatImpl.class);
    private final RestClient restClient;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String STATS_SERVICE_NAME = "STATS-SERVER";

    public ClientRestStatImpl(@LoadBalanced RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl("http://" + STATS_SERVICE_NAME)
                .build();
    }

    @Override
    public Boolean addStat(EndpointHitDto dto) {
        try {
            logger.info("Sending POST request to stats-service/hit with dto: {}", dto);
            return restClient.post()
                    .uri("/hit")
                    .body(dto)
                    .retrieve()
                    .body(Boolean.class);
        } catch (Exception e) {
            logger.error("Error while sending POST request to stats-service/hit: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public List<ViewStatsDto> getStat(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start date must not be after end date");
        }

        URI uri = buildStatsUri(start, end, uris, unique);
        logger.info("Sending GET request to stats-service with URI: {}", uri);

        try {
            ResponseEntity<ViewStatsDto[]> responseEntity = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .toEntity(ViewStatsDto[].class);

            return responseEntity.getBody() != null
                    ? Arrays.asList(responseEntity.getBody())
                    : Collections.emptyList();
        } catch (Exception e) {
            logger.error("Error while fetching stats from stats-service: {} - {}", uri, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private URI buildStatsUri(LocalDateTime start,
                              LocalDateTime end,
                              List<String> uris,
                              boolean unique) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/stats")
                .queryParam("start", formatDateTime(start))
                .queryParam("end", formatDateTime(end))
                .queryParam("unique", unique);

        if (Objects.nonNull(uris) && !uris.isEmpty()) {
            for (String uri : uris) {
                builder.queryParam("uris", uri);
            }
        }
        return builder.build().toUri();
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(FORMATTER);
    }
}