package ru.practicum.event.filter;

import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.practicum.enums.EventState;

import java.util.List;

@SuperBuilder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
public class EventFilterAdmin extends EventFilterBase {

    // admin
    private List<Long> users;

    // admin
    private List<EventState> states;

}
