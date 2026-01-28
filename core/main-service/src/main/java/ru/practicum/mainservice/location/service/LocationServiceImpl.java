package ru.practicum.mainservice.location.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.location.Location;
import ru.practicum.mainservice.location.LocationRepository;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class LocationServiceImpl implements LocationService {

    private LocationRepository locationRepository;

    @Override
    public Optional<Location> findByLatAndLon(final BigDecimal lat, final BigDecimal lon) {
        return locationRepository.findByLatAndLon(lat, lon);
    }

    @Override
    public Location save(final Location location) {
        return locationRepository.save(location);
    }
}
