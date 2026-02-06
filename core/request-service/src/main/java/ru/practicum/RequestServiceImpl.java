package ru.practicum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.enums.EventState;
import ru.practicum.event.Event;
import ru.practicum.event.dto.EventDtoFull;
import ru.practicum.exception.*;
import ru.practicum.feign.EventRepository;
import ru.practicum.feign.UserRepository;
import ru.practicum.request.dto.*;
import ru.practicum.request.Request;
import ru.practicum.request.RequestStatus;
import ru.practicum.user.User;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final RequestMapper requestMapper;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public RequestDto createRequest(Long userId, Long eventId) throws UserNotFoundException, EventNotFoundException, RequestAlreadyExistsException, ParticipantLimitExceededException, RequestSelfAttendException, EventNotPublishedException {
        log.info("Main-service. createRequest input: userId = {}, eventId = {}", userId, eventId);

        User user = userRepository.findById(userId);

        Event event = toEventFromFullDto(eventRepository.findById(eventId));

        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new RequestAlreadyExistsException("Request for eventId = %d by userId = %d already exists".formatted(eventId, userId));
        }

        if (event.getInitiatorId().equals(userId)) {
            throw new RequestSelfAttendException("Cannot request to your own event");
        }

        if (event.getState() != EventState.PUBLISHED) {
            throw new EventNotPublishedException("event is not published yet");
        }

        if (event.getParticipantLimit() != 0) {
            Long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
            if (event.getParticipantLimit() > 0 && confirmedCount >= event.getParticipantLimit()) {
                throw new ParticipantLimitExceededException("Participant limit " + event.getParticipantLimit() + " exceeded for event " + eventId);
            }
            event.setConfirmedRequests(confirmedCount);
        }

        RequestStatus status = determineRequestStatus(event);

        Request request = Request.builder()
                .created(LocalDateTime.now())
                .eventId(event.getId())
                .requesterId(user.getId())
                .status(status)
                .build();

        Request savedRequest = requestRepository.save(request);

        log.info("Main-service. createRequest success {}", request.getId());

        return requestMapper.toDto(savedRequest);
    }

    private RequestStatus determineRequestStatus(Event event) {
        // Если модерация отключена или лимит не установлен, то автоматическое подтверждение
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            return RequestStatus.CONFIRMED;
        }
        return RequestStatus.PENDING;
    }

    @Override
    @Transactional
    public RequestStatusUpdateResultDto updateRequests(Long userId, Long eventId, RequestStatusUpdateDto requestStatusUpdateDto) throws EventNotFoundException, EventNotPublishedException, ParticipantLimitExceededException {
        //        если для события лимит заявок равен 0 или отключена пре-модерация заявок, то подтверждение заявок не требуется
        //        нельзя подтвердить заявку, если уже достигнут лимит по заявкам на данное событие (Ожидается код ошибки 409)
        //        статус можно изменить только у заявок, находящихся в состоянии ожидания (Ожидается код ошибки 409)
        //        если при подтверждении данной заявки, лимит заявок для события исчерпан, то все неподтверждённые заявки необходимо отклонить
        log.info("Main-service. updateRequests input: userId = {}, eventId = {}, RequestStatusUpdateDto = {}", userId, eventId, requestStatusUpdateDto);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId);

        if (!event.getRequestModeration() && requestStatusUpdateDto == null) {
            return new RequestStatusUpdateResultDto();
        }

        if (event.getState() != EventState.PUBLISHED) {
            throw new EventNotPublishedException("event is not published");
        }

        Long confirmedCount = requestRepository.countConfirmedRequests(eventId);
        if (event.getParticipantLimit() > 0 && confirmedCount >= event.getParticipantLimit()) {
            throw new ParticipantLimitExceededException("Participant limit exceeded");
        }

        requestRepository.updateRequestsStatus(
                requestStatusUpdateDto.getRequestIds(),
                eventId,
                requestStatusUpdateDto.getStatus()
        );

        List<Request> confirmedRequests = requestRepository.findRequestsByStatus(
                requestStatusUpdateDto.getRequestIds(),
                eventId,
                RequestStatus.CONFIRMED
        );

        List<Request> rejectedRequests = requestRepository.findRequestsByStatus(
                requestStatusUpdateDto.getRequestIds(),
                eventId,
                RequestStatus.REJECTED
        );

        List<ParticipationRequestDto> confirmedRequestsList = requestMapper.toParticipationDtoList(confirmedRequests);
        List<ParticipationRequestDto> rejectedRequestsList = requestMapper.toParticipationDtoList(rejectedRequests);
        log.info("Main-service. updateRequests success: confirmedRequests = {}, rejectedRequests = {}", confirmedRequestsList.size(), rejectedRequestsList.size());

        return RequestStatusUpdateResultDto.builder()
                .confirmedRequests(confirmedRequestsList)
                .rejectedRequests(rejectedRequestsList)
                .build();
    }

    @Override
    @Transactional
    public RequestDto cancelRequests(Long userId, Long requestId) throws RequestNotFoundException {
        log.info("Main-service. cancelRequests input: userId = {}, requestId = {}", userId, requestId);
        Request request = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new RequestNotFoundException("Request not found"));

        request.setStatus(RequestStatus.CANCELED);
        Request updatedRequest = requestRepository.save(request);

        log.info("Main-service. cancelRequests success: id = {}", updatedRequest.getId());

        return requestMapper.toDto(updatedRequest);
    }

    @Override
    public List<RequestDto> getCurrentUserRequests(Long userId) throws UserNotFoundException {
        log.info("Main-service. getCurrentUserRequests input: userId = {}", userId);

        userRepository.findById(userId);

        List<Request> requests = requestRepository.findByRequesterId(userId);

        log.info("Main-service. getCurrentUserRequests success: size = {}", requests.size());

        return requestMapper.toDtoList(requests);
    }

    @Override
    public List<RequestDto> getRequestsByOwnerOfEvent(Long userId, Long eventId) throws EventNotFoundException {
        log.info("Main-service. getRequestsByOwnerOfEvent input: userId = {}, eventId = {}", userId, eventId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId);

        List<Request> requests = requestRepository.findByEventIdAndEventInitiatorId(eventId, userId);

        log.info("Main-service. getRequestsByOwnerOfEvent success: size = {}", requests.size());

        return requestMapper.toDtoList(requests);
    }

    @Override
    public List<ConfirmedRequestsCount> findConfirmedRequestsCountByEventIds(final List<Long> eventIds) {
        return requestRepository.findConfirmedRequestsCountByEventIds(eventIds);
    }

    @Override
    public Long countConfirmedRequests(final Long eventId) {
        return requestRepository.countConfirmedRequests(eventId);
    }


    // Event из EventDtoFull (полное преобразование)
    private Event toEventFromFullDto(EventDtoFull eventDtoFull) {
        Event event = new Event();

        // Базовые поля
        event.setId(eventDtoFull.getId());
        event.setAnnotation(eventDtoFull.getAnnotation());
        event.setDescription(eventDtoFull.getDescription());
        event.setEventDate(eventDtoFull.getEventDate());
        event.setPaid(eventDtoFull.getPaid());
        event.setParticipantLimit(eventDtoFull.getParticipantLimit());
        event.setRequestModeration(eventDtoFull.getRequestModeration());
        event.setTitle(eventDtoFull.getTitle());
        event.setState(eventDtoFull.getState());
        event.setViews(eventDtoFull.getViews());
        event.setConfirmedRequests(eventDtoFull.getConfirmedRequests());
        event.setCreatedOn(eventDtoFull.getCreatedOn());
        event.setPublishedOn(eventDtoFull.getPublishedOn());

        // Преобразование вложенных объектов в ID
        if (eventDtoFull.getCategory() != null) {
            event.setCategoryId(eventDtoFull.getCategory().getId());
        }

        if (eventDtoFull.getInitiator() != null) {
            event.setInitiatorId(eventDtoFull.getInitiator().getId());
        }

        if (eventDtoFull.getLocation() != null) {
            event.setLocationId(eventDtoFull.getLocation().getId());
        }

        return event;
    }
}
