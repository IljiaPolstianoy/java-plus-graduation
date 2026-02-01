package ru.practicum.mainservice.location.service;

import ru.practicum.location.Location;

import java.math.BigDecimal;
import java.util.Optional;

public interface LocationService {

    Optional<Location> findByLatAndLon(BigDecimal lat, BigDecimal lon);

    Location save(Location location);
}
