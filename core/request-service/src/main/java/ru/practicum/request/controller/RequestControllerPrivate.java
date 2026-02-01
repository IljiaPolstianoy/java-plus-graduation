package ru.practicum.request.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.exception.*;
import ru.practicum.request.RequestService;
import ru.practicum.request.dto.RequestDto;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
@Validated
public class RequestControllerPrivate {

    private final RequestService requestService;

    @GetMapping
    public List<RequestDto> getParticipationRequest(@PathVariable @Positive Long userId) throws UserNotFoundException {
        return requestService.getCurrentUserRequests(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RequestDto participationRequest(@PathVariable @Positive Long userId, @RequestParam @Positive Long eventId) throws UserNotFoundException, ParticipantLimitExceededException, EventNotFoundException, RequestAlreadyExistsException, RequestSelfAttendException, EventNotPublishedException {
        return requestService.createRequest(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    public RequestDto cancelParticipationRequest(@PathVariable @Positive Long userId, @PathVariable @Positive Long requestId) throws RequestNotFoundException {
        return requestService.cancelRequests(userId, requestId);
    }
}
