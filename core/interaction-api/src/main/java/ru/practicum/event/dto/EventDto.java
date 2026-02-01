package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;
import ru.practicum.event.enums.EventStateAction;
import ru.practicum.location.LocationDto;
import ru.practicum.validation.ValidationGroups;

import java.time.LocalDateTime;

/**
 * EvenDto входная для контролллера
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDto {

    @Null(groups = ValidationGroups.Create.class)
    private Long id;

    @NotBlank(groups = ValidationGroups.Create.class)
    @Size(min = 20, max = 2000)
    private String annotation;

    @NotNull(groups = ValidationGroups.Create.class)
    @Positive
    private Long category;

    @NotBlank(groups = ValidationGroups.Create.class)
    @Size(min = 20, max = 7000)
    private String description;

    @NotNull(groups = ValidationGroups.Create.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private Long initiator;

    @NotNull(groups = ValidationGroups.Create.class)
    LocationDto location;

    private Boolean paid;

    @PositiveOrZero
    private Integer participantLimit;

    private Boolean requestModeration;

    private EventStateAction stateAction;

    @NotNull(groups = ValidationGroups.Create.class)
    @Size(min = 3, max = 120)
    private String title;

    private Long views;

}
