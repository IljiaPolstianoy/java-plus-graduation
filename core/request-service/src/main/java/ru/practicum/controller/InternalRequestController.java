package ru.practicum.controller;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.RequestService;
import ru.practicum.dto.ConfirmedRequestsCount;
import ru.practicum.dto.RequestDto;
import ru.practicum.dto.RequestStatusUpdateDto;
import ru.practicum.dto.RequestStatusUpdateResultDto;

import java.util.List;

@RestController
@RequestMapping("/internal/request")
@RequiredArgsConstructor
@Validated
public class InternalRequestController {

    private final RequestService requestService;

    @GetMapping("/user/{userId}/event/{eventId}")
    public List<RequestDto> getRequestsByOwnerOfEvent(
            @PathVariable @Positive final Long userId,
            @PathVariable @Positive final Long eventId
    ) {
        return requestService.getRequestsByOwnerOfEvent(userId, eventId);
    }

    @PostMapping("/user/{userId}/event/{eventId}")
    public RequestStatusUpdateResultDto updateRequest(
            @PathVariable @Positive final Long userId,
            @PathVariable @Positive final Long eventId,
            @RequestBody @Validated final RequestStatusUpdateDto requestStatusUpdateDto
    ) {
        return requestService.updateRequests(userId, eventId, requestStatusUpdateDto);
    }

    @PostMapping("/confirmeds")
    public List<ConfirmedRequestsCount> findConfirmedRequestsCountByEventIds(
            final @NotEmpty @RequestBody List<Long> eventIds
    ) {
        return requestService.findConfirmedRequestsCountByEventIds(eventIds);
    }

    @PostMapping("/confirmed")
    public Long countConfirmedRequests(final @NotNull @RequestBody Long eventId) {
        return requestService.countConfirmedRequests(eventId);
    }
}
