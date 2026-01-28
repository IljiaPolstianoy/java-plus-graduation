package ru.practicum.mainservice.request.controller;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.request.RequestService;
import ru.practicum.request.dto.ConfirmedRequestsCount;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.dto.RequestStatusUpdateDto;
import ru.practicum.request.dto.RequestStatusUpdateResultDto;

import java.util.List;

@Controller
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

    @PatchMapping("/user/{userId}/event/{eventId}")
    public RequestStatusUpdateResultDto updateRequest(
            @PathVariable @Positive final Long userId,
            @PathVariable @Positive final Long eventId,
            @RequestBody @Validated final RequestStatusUpdateDto requestStatusUpdateDto
    ) {
        return requestService.updateRequests(userId, eventId, requestStatusUpdateDto);
    }

    @GetMapping("/confirmeds")
    public List<ConfirmedRequestsCount> findConfirmedRequestsCountByEventIds(
            final @NotEmpty List<Long> eventIds
    ) {
        return requestService.findConfirmedRequestsCountByEventIds(eventIds);
    }

    @GetMapping("/confirmed")
    public Long countConfirmedRequests(final @NotNull Long eventId) {
        return requestService.countConfirmedRequests(eventId);
    }
}
