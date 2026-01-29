package ru.practicum.mainservice.location;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.config.CommonMapperConfiguration;
import ru.practicum.location.Location;
import ru.practicum.location.LocationDto;

@Mapper(config = CommonMapperConfiguration.class)
public interface LocationMapper {
    @Mapping(target = "id", ignore = true)
    LocationDto toLocationDto(Location location);

    Location toLocation(LocationDto locationDto);
}
