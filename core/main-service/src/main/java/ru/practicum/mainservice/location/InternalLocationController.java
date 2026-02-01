package ru.practicum.mainservice.location;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.location.Location;
import ru.practicum.mainservice.location.service.LocationService;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/internal/location")
public class InternalLocationController {

    private final LocationService locationService;

    @GetMapping("/{lat}/{lon}")
    public Location getLocation(
            @PathVariable("lat") @NotNull final BigDecimal lat,
            @PathVariable("lon") @NotNull final BigDecimal lon
    ) {
        return locationService.findByLatAndLon(lat, lon)
                .orElseGet(() -> {
                    Location newLocation = new Location();
                    newLocation.setLat(lat);
                    newLocation.setLon(lon);
                    return locationService.save(newLocation);
                });
    }

    @PostMapping
    public Location save(@RequestBody @Valid final Location location) {
        return locationService.save(location);
    }
}
