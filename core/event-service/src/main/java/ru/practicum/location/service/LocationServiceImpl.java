package ru.practicum.location.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.exception.LocationNotFound;
import ru.practicum.location.Location;
import ru.practicum.location.LocationRepository;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;

    @Override
    public Optional<Location> findByLatAndLon(final BigDecimal lat, final BigDecimal lon) {
        return locationRepository.findByLatAndLon(lat, lon);
    }

    @Override
    public Location save(final Location location) {
        return locationRepository.save(location);
    }

    @Override
    public Location getLocationById(final Long locationId) {
        return locationRepository.findById(locationId)
                .orElseThrow(() -> new LocationNotFound("Location with id " + locationId + " not found"));
    }
}
