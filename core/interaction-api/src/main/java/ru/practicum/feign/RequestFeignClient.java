package ru.practicum.feign;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.request.dto.ConfirmedRequestsCount;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.dto.RequestStatusUpdateDto;
import ru.practicum.request.dto.RequestStatusUpdateResultDto;

import java.util.List;

@FeignClient(
        name = "request-service",
        contextId = "requestFeign",
        path = "/internal/request"
)
public interface RequestFeignClient {

    @GetMapping("/user/{userId}/event/{eventId}")
    List<RequestDto> getRequestsByOwnerOfEvent(
            @PathVariable @Positive final Long userId,
            @PathVariable @Positive final Long eventId
    );

    @PatchMapping("/user/{userId}/event/{eventId}")
    RequestStatusUpdateResultDto updateRequest(
            @PathVariable @Positive final Long userId,
            @PathVariable @Positive final Long eventId,
            @RequestBody @Validated final RequestStatusUpdateDto requestStatusUpdateDto
    );

    @GetMapping("/confirmeds")
    List<ConfirmedRequestsCount> findConfirmedRequestsCountByEventIds(final @NotEmpty List<Long> eventIds);

    @GetMapping("/confirmed")
    Long countConfirmedRequests(final @NotNull Long eventId);
}
