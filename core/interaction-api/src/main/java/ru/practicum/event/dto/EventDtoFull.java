package ru.practicum.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.category.CategoryDto;
import ru.practicum.event.enums.EventState;
import ru.practicum.location.LocationDto;
import ru.practicum.user.dto.UserDto;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class EventDtoFull {
    private Long id;

    @NotBlank
    @Size(min = 20, max = 2000)
    private String annotation;

    private CategoryDto category;
    private Long confirmedRequests;
    private LocalDateTime createdOn;
    private String description;

    private LocalDateTime eventDate;

    private UserDto initiator;
    LocationDto location;
    private Boolean paid;
    private Integer participantLimit;

    private LocalDateTime publishedOn;

    private Boolean requestModeration;
    private EventState state;
    private String title;
    private Long views;

}
