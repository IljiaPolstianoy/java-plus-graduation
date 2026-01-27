package ru.practicum.mainservice.request;

import ru.practicum.exception.*;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.dto.RequestStatusUpdateDto;
import ru.practicum.request.dto.RequestStatusUpdateResultDto;

import java.util.List;

public interface RequestService {

    List<RequestDto> getRequestsByOwnerOfEvent(Long userId, Long eventId) throws EventNotFoundException;

    RequestStatusUpdateResultDto updateRequests(Long userId, Long eventId, RequestStatusUpdateDto requestStatusUpdateDto) throws EventNotFoundException, EventNotPublishedException, ParticipantLimitExceededException;

    RequestDto createRequest(Long userId, Long eventId) throws UserNotFoundException, EventNotFoundException, RequestAlreadyExistsException, ParticipantLimitExceededException, RequestSelfAttendException, EventNotPublishedException;

    List<RequestDto> getCurrentUserRequests(Long userId) throws UserNotFoundException;

    RequestDto cancelRequests(Long userId, Long requestId) throws RequestNotFoundException;

}
