package ru.practicum.feign;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.dto.ConfirmedRequestsCount;
import ru.practicum.dto.RequestDto;
import ru.practicum.dto.RequestStatusUpdateDto;
import ru.practicum.dto.RequestStatusUpdateResultDto;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RequestService {

    private final RequestFeignClient requestFeignClient;

    public List<RequestDto> getRequestsByOwnerOfEvent(
            final Long userId,
            final Long eventId
    ) {
        return requestFeignClient.getRequestsByOwnerOfEvent(userId, eventId);
    }

    public RequestStatusUpdateResultDto updateRequests(
            final Long userId,
            final Long eventId,
            final RequestStatusUpdateDto requestStatusUpdateDto
    ) {
        return requestFeignClient.updateRequest(userId, eventId, requestStatusUpdateDto);
    }

    public List<ConfirmedRequestsCount> findConfirmedRequestsCountByEventIds(final List<Long> eventIds) {
        return requestFeignClient.findConfirmedRequestsCountByEventIds(eventIds);
    }

    public Long countConfirmedRequests(final Long eventId) {
        return requestFeignClient.countConfirmedRequests(eventId);
    }
}