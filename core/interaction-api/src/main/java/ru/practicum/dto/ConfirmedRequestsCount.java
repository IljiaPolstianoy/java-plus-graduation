package ru.practicum.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ConfirmedRequestsCount {
    private final Long eventId;
    private final Long count;
}
