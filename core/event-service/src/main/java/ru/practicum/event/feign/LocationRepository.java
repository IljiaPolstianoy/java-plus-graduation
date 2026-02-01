package ru.practicum.event.feign;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import ru.practicum.feign.LocationFeignClient;
import ru.practicum.location.Location;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class LocationRepository {

    private final LocationFeignClient locationFeignClient;

    public Location findByLatAndLon(final BigDecimal lat, final BigDecimal lon) {
        return locationFeignClient.getLocation(lat, lon);
    }

    public Location save(final Location location) {
        return locationFeignClient.save(location);
    }
}
