package ru.practicum.feign;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.location.Location;

import java.math.BigDecimal;

@FeignClient(
        name = "main-service",
        contextId = "locationFeign",
        path = "/internal/location"
)
public interface LocationFeignClient {

    @GetMapping("/{lat}/{lon}")
    Location getLocation(
            @PathVariable("lat") @NotNull final BigDecimal lat,
            @PathVariable("lon") @NotNull final BigDecimal lon
    );

    @PostMapping
    Location save(@RequestBody @Valid final Location location);
}
