package ru.practicum.event;

import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Builder
@Getter
public class RequestAllEvent {

    private Set<Long> ids;
}
