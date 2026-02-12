package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import ru.practicum.enums.EventState;
import ru.practicum.enums.EventStateAction;
import ru.practicum.event.Event;
import ru.practicum.event.EventMapper;
import ru.practicum.event.EventRepository;
import ru.practicum.event.EventSpecifications;
import ru.practicum.event.dto.EventDto;
import ru.practicum.event.dto.EventDtoFull;
import ru.practicum.event.filter.EventFilterAdmin;
import ru.practicum.event.filter.EventFilterBase;
import ru.practicum.event.filter.EventFilterPublic;
import ru.practicum.exception.*;
import ru.practicum.feign.RequestService;
import ru.practicum.location.Location;
import ru.practicum.location.LocationRepository;
import ru.practicum.request.dto.ConfirmedRequestsCount;
import ru.practicum.stats.AnalyzerGrpcClient;
import ru.practicum.stats.CollectorGrpcClient;
import ru.practicum.stats.RecommendedEvent;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final Validator validator;
    private final LocationRepository locationRepository;
    private final RequestService requestService;
    private final CollectorGrpcClient collectorGrpcClient;
    private final AnalyzerGrpcClient analyzerGrpcClient;

    /**
     * Отправка регистрации в Collector через gRPC (для request-service)
     */
    public void sendRegisterAction(final Long userId, final Long eventId) {
        try {
            collectorGrpcClient.sendRegisterAction(userId, eventId, LocalDateTime.now());
            log.debug("Register action sent: user={}, event={}", userId, eventId);
        } catch (Exception e) {
            log.error("Failed to send register action: {}", e.getMessage());
        }
    }

    @Override
    public List<EventDtoFull> findEventsByUsers(final EventFilterAdmin eventFilter) throws FilterValidationException, EventDateException {
        validateFilter(eventFilter);

        log.info("Main-service. findEventsByUsers input: filter = {}", eventFilter);

        final Specification<Event> specification = EventSpecifications.forAdminFilter(eventFilter);
        final Pageable pageable = PageRequest.of(eventFilter.getFrom() / eventFilter.getSize(), eventFilter.getSize());
        final Page<Event> pageEvents = eventRepository.findAll(specification, pageable);
        final List<EventDtoFull> events = getDtoFullList(pageEvents);

        enrichEventsWithConfirmedRequests(events);
        enrichEventsWithRatings(events);

        log.info("Main-service. findEventsByUsers success: size = {}", events.size());
        return events;
    }

    @Override
    @Transactional
    public EventDtoFull updateEventById(final EventDto eventDto) throws EventNotFoundException, EventDateException, EventAlreadyPublishedException, EventCanceledCantPublishException {
        log.info("Main-service. updateEventById input: id = {}", eventDto.getId());
        final Event event = eventRepository.findById(eventDto.getId())
                .orElseThrow(() -> new EventNotFoundException("event with id=%d was not found".formatted(eventDto.getId())));

        if (eventDto.getEventDate() != null && !eventDto.getEventDate().isAfter(LocalDateTime.now().plusHours(1))) {
            throw new EventDateException("event date should be in 1+ hours after now");
        }

        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new EventAlreadyPublishedException("event is already published");
        }

        if (event.getState().equals(EventState.CANCELED)) {
            throw new EventCanceledCantPublishException("Canceled event cant be published");
        }

        if (eventDto.getStateAction() != null) {
            if (eventDto.getStateAction().equals(EventStateAction.PUBLISH_EVENT)) {
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            }
            if (eventDto.getStateAction().equals(EventStateAction.REJECT_EVENT)) {
                event.setState(EventState.CANCELED);
            }
        }

        eventMapper.updateEventFromDto(eventDto, event);
        final Event updatedEvent = eventRepository.save(event);

        log.info("Main-service. updateEventById success: id = {}", updatedEvent.getId());
        return eventMapper.toEventFullDto(updatedEvent);
    }

    @Override
    @Transactional
    public EventDtoFull createEvent(EventDto eventDto) throws
            CategoryNotFoundException, UserNotFoundException, EventDateException {
        log.info("Main-service. createEvent input: {}", eventDto.getDescription());

        setDefaultValues(eventDto);

        if (eventDto.getEventDate() != null && !eventDto.getEventDate().isAfter(LocalDateTime.now().plusHours(1))) {
            throw new EventDateException("event date should be in 1+ hours after now");
        }

        Event event = eventMapper.toEvent(eventDto);
        if (eventDto.getCategory() != null) {
            event.setCategoryId(eventDto.getCategory());
        }

        if (eventDto.getInitiator() != null) {
            event.setInitiatorId(eventDto.getInitiator());
        }

        if (eventDto.getLocation() != null) {
            Location location = locationRepository.findByLatAndLon(
                            eventDto.getLocation().getLat(),
                            eventDto.getLocation().getLon()
                    )
                    .orElseGet(() ->
                            locationRepository.save(
                                    Location.builder()
                                            .lat(eventDto.getLocation().getLat())
                                            .lon(eventDto.getLocation().getLon())
                                            .build())
                    );
            event.setLocationId(location.getId());
        }

        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now());
        event.setConfirmedRequests(0L);
        event.setRating(0.0);

        Event createdEvent = eventRepository.save(event);

        log.info("Main-service. createEvent success: id = {}", createdEvent.getId());
        return eventMapper.toEventFullDto(createdEvent);
    }

    @Override
    public EventDtoFull findEventByUserId(Long userId, Long eventId) throws EventNotFoundException {
        log.info("Main-service. findEventByUserId input: userId = {}, eventId = {}", userId, eventId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new EventNotFoundException("event with id " + eventId + " not found"));

        EventDtoFull eventDto = eventMapper.toEventFullDto(event);
        enrichEventWithAdditionalData(eventDto);

        log.info("Main-service. findEventByUserId success: id = {}", event.getId());
        return eventDto;
    }

    @Override
    public List<EventDtoFull> findEventsByUserid(Long userId, int from, int size) {
        log.info("Main-service. findEventsByUserid input: userId = {}, from = {}, size = {}", userId, from, size);

        Pageable pageable = PageRequest.of(from / size, size);
        Page<Event> pageEvents = eventRepository.findAllByInitiatorId(userId, pageable);
        final List<EventDtoFull> events = getDtoFullList(pageEvents);

        enrichEventsWithConfirmedRequests(events);
        enrichEventsWithRatings(events);

        log.info("Main-service. findEventsByUserid success: size = {}", events.size());
        return events;
    }

    @Override
    @Transactional
    public EventDtoFull updateEventByUserId(EventDto eventDto) throws EventNotFoundException, EventDateException, EventCanceledCantPublishException {
        log.info("Main-service. updateEventByUserId input: eventId = {}, userId = {}", eventDto.getId(), eventDto.getInitiator());

        if (eventDto.getEventDate() != null && !eventDto.getEventDate().isAfter(LocalDateTime.now().plusHours(2))) {
            throw new EventDateException("event date should be in 2+ hours after now");
        }

        Event existingEvent = eventRepository.findByIdAndInitiatorId(eventDto.getId(), eventDto.getInitiator())
                .orElseThrow(() -> new EventNotFoundException("event with id " + eventDto.getId() + " not found"));

        if (!(existingEvent.getState() == EventState.PENDING || existingEvent.getState() == EventState.CANCELED)) {
            throw new EventCanceledCantPublishException("event can be edited only Pending or Canceled");
        }

        eventMapper.updateEventFromDto(eventDto, existingEvent);

        if (eventDto.getStateAction() != null) {
            if (eventDto.getStateAction().equals(EventStateAction.SEND_TO_REVIEW)) {
                existingEvent.setState(EventState.PENDING);
            } else {
                existingEvent.setState(EventState.CANCELED);
            }
        }
        Event updatedEvent = eventRepository.save(existingEvent);

        log.info("Main-service. updateEventByUserId success: eventId = {}", updatedEvent.getId());
        return eventMapper.toEventFullDto(updatedEvent);
    }

    @Override
    public List<EventDtoFull> findEvents(EventFilterPublic eventFilter, HttpServletRequest request) throws
            FilterValidationException, EventDateException {
        validateFilter(eventFilter);

        log.info("Main-service. findEvents input: filter = {}", eventFilter);

        Specification<Event> specification = EventSpecifications.forPublicFilter(eventFilter);
        Pageable pageable = PageRequest.of(eventFilter.getFrom() / eventFilter.getSize(), eventFilter.getSize());
        Page<Event> pageEvents = eventRepository.findAll(specification, pageable);
        final List<EventDtoFull> events = getDtoFullList(pageEvents);

        enrichEventsWithConfirmedRequests(events);
        enrichEventsWithRatings(events);

        log.info("Main-service. findEvents success: size = {}", events.size());
        return events;
    }

    @Override
    public EventDtoFull findEventById(Long eventId, HttpServletRequest request) throws EventNotFoundException {
        log.info("Main-service. findEventById input: eventId = {}", eventId);

        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new EventNotFoundException("event with id " + eventId + " not found"));

        String userIdHeader = request.getHeader("X-EWM-USER-ID");
        if (userIdHeader != null) {
            try {
                Long userId = Long.parseLong(userIdHeader);
                sendViewAction(userId, eventId);
            } catch (NumberFormatException e) {
                log.warn("Invalid X-EWM-USER-ID header: {}", userIdHeader);
            }
        }

        EventDtoFull eventDto = eventMapper.toEventFullDto(event);
        enrichEventWithAdditionalData(eventDto);

        log.info("Main-service. findEventById success: eventId = {}", event.getId());
        return eventDto;
    }

    /**
     * PUT /events/{eventId}/like - отправка лайка
     */
    @Override
    @Transactional
    public EventDtoFull likeEvent(Long eventId, HttpServletRequest request) throws EventNotFoundException {
        log.info("Main-service. likeEvent input: eventId = {}", eventId);

        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new EventNotFoundException("event with id " + eventId + " not found"));

        String userIdHeader = request.getHeader("X-EWM-USER-ID");
        if (userIdHeader == null) {
            throw new IllegalArgumentException("X-EWM-USER-ID header is required");
        }

        Long userId = Long.parseLong(userIdHeader);

        boolean hasParticipated = requestService.existsConfirmedRequestByRequesterIdAndEventId(userId, eventId);

        if (!hasParticipated) {
            throw new IllegalArgumentException("User can only like events they have participated in");
        }

        sendLikeAction(userId, eventId);

        EventDtoFull eventDto = eventMapper.toEventFullDto(event);
        enrichEventWithAdditionalData(eventDto);

        return eventDto;
    }

    /**
     * GET /events/recommendations - получение рекомендаций
     */
    @Override
    public List<EventDtoFull> getRecommendations(HttpServletRequest request) {
        log.info("Main-service. getRecommendations");

        String userIdHeader = request.getHeader("X-EWM-USER-ID");
        if (userIdHeader == null) {
            log.warn("X-EWM-USER-ID header is missing");
            return Collections.emptyList();
        }

        Long userId = Long.parseLong(userIdHeader);
        List<Long> recommendedEventIds = getRecommendationsForUser(userId, 10);

        if (recommendedEventIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Event> events = eventRepository.findAllById(recommendedEventIds);
        Map<Long, Event> eventMap = events.stream()
                .collect(Collectors.toMap(Event::getId, e -> e));

        List<EventDtoFull> result = recommendedEventIds.stream()
                .map(eventMap::get)
                .filter(Objects::nonNull)
                .map(eventMapper::toEventFullDto)
                .collect(Collectors.toList());

        enrichEventsWithConfirmedRequests(result);
        enrichEventsWithRatings(result);

        log.info("Main-service. getRecommendations success: size = {}", result.size());
        return result;
    }

    /**
     * GET /events/{eventId}/similar - получение похожих мероприятий
     */
    @Override
    public List<EventDtoFull> getSimilarEvents(Long eventId, HttpServletRequest request) {
        log.info("Main-service. getSimilarEvents input: eventId = {}", eventId);

        String userIdHeader = request.getHeader("X-EWM-USER-ID");
        Long userId = userIdHeader != null ? Long.parseLong(userIdHeader) : null;

        List<Long> similarEventIds = getSimilarEvents(eventId, userId, 10);

        if (similarEventIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Event> events = eventRepository.findAllById(similarEventIds);
        Map<Long, Event> eventMap = events.stream()
                .collect(Collectors.toMap(Event::getId, e -> e));

        List<EventDtoFull> result = similarEventIds.stream()
                .map(eventMap::get)
                .filter(Objects::nonNull)
                .map(eventMapper::toEventFullDto)
                .collect(Collectors.toList());

        enrichEventsWithConfirmedRequests(result);
        enrichEventsWithRatings(result);

        log.info("Main-service. getSimilarEvents success: size = {}", result.size());
        return result;
    }

    @Override
    public EventDtoFull findById(final Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Событие не найдено с ID %d".formatted(eventId)));

        EventDtoFull eventDto = eventMapper.toEventFullDto(event);
        enrichEventWithAdditionalData(eventDto);

        return eventDto;
    }

    @Override
    public Optional<Event> findByIdAndInitiatorId(final Long eventId, final Long userId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId);
    }

    @Override
    public boolean existsByCategoryId(final Long categoryId) {
        return eventRepository.existsByCategoryId(categoryId);
    }

    @Override
    public boolean existsById(final Long eventId) {
        return eventRepository.existsById(eventId);
    }

    @Override
    public List<Event> findAllById(final Set<Long> ids) {
        return eventRepository.findAllById(ids);
    }

    @Override
    public Page<Event> findAllByInitiatorIdIn(List<Long> userIds, Pageable pageable) {
        return eventRepository.findAllByInitiatorIdIn(userIds, pageable);
    }

    @Override
    public void delete(final Long eventId) {
        eventRepository.deleteById(eventId);
    }

    private void validateFilter(EventFilterBase filter) throws FilterValidationException, EventDateException {
        validateDateRange(filter.getRangeStart(), filter.getRangeEnd());

        Errors errors = new BeanPropertyBindingResult(filter, "filter");
        ValidationUtils.invokeValidator(validator, filter, errors);

        if (errors.hasErrors()) {
            String errorMessage = errors.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(", "));

            throw new FilterValidationException("Filter validation failed: " + errorMessage);
        }
    }

    private void validateDateRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) throws EventDateException {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new EventDateException("Start date must be before end date");
        }
    }

    private void setDefaultValues(EventDto eventDto) {
        if (eventDto.getPaid() == null) {
            eventDto.setPaid(false);
        }
        if (eventDto.getRequestModeration() == null) {
            eventDto.setRequestModeration(true);
        }
        if (eventDto.getParticipantLimit() == null) {
            eventDto.setParticipantLimit(0);
        }
    }

    private List<EventDtoFull> getDtoFullList(Page<Event> pageEvents) {
        return pageEvents.stream()
                .map(eventMapper::toEventFullDto)
                .collect(Collectors.toList());
    }


    /**
     * Отправка просмотра в Collector через gRPC
     */
    private void sendViewAction(Long userId, Long eventId) {
        try {
            collectorGrpcClient.sendViewAction(userId, eventId, LocalDateTime.now());
            log.debug("View action sent: user={}, event={}", userId, eventId);
        } catch (Exception e) {
            log.error("Failed to send view action: {}", e.getMessage());
        }
    }

    /**
     * Отправка лайка в Collector через gRPC
     */
    private void sendLikeAction(Long userId, Long eventId) {
        try {
            collectorGrpcClient.sendLikeAction(userId, eventId, LocalDateTime.now());
            log.debug("Like action sent: user={}, event={}", userId, eventId);
        } catch (Exception e) {
            log.error("Failed to send like action: {}", e.getMessage());
        }
    }

    /**
     * Получение рейтинга мероприятия из Analyzer через gRPC
     */
    private Double getEventRating(Long eventId) {
        try {
            List<RecommendedEvent> interactions = analyzerGrpcClient.getInteractionsCount(List.of(eventId));
            if (!interactions.isEmpty()) {
                return interactions.getFirst().getScore();
            }
        } catch (Exception e) {
            log.error("Failed to get event rating for {}: {}", eventId, e.getMessage());
        }
        return 0.0;
    }

    /**
     * Получение рекомендаций для пользователя из Analyzer
     */
    private List<Long> getRecommendationsForUser(Long userId, int limit) {
        try {
            return analyzerGrpcClient.getRecommendationsForUser(userId, limit).stream()
                    .map(RecommendedEvent::getEventId)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get recommendations for user {}: {}", userId, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Получение похожих мероприятий из Analyzer
     */
    private List<Long> getSimilarEvents(Long eventId, Long userId, int limit) {
        try {
            return analyzerGrpcClient.getSimilarEvents(eventId, userId, limit).stream()
                    .map(RecommendedEvent::getEventId)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get similar events for event {}: {}", eventId, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Обогащение событий подтвержденными запросами
     */
    private void enrichEventsWithConfirmedRequests(List<EventDtoFull> events) {
        if (events.isEmpty()) {
            return;
        }
        List<Long> eventIds = events.stream()
                .map(EventDtoFull::getId)
                .collect(Collectors.toList());

        List<ConfirmedRequestsCount> results = requestService.findConfirmedRequestsCountByEventIds(eventIds);
        Map<Long, Long> confirmedRequestsMap = results.stream()
                .collect(Collectors.toMap(
                        ConfirmedRequestsCount::getEventId,
                        ConfirmedRequestsCount::getCount
                ));

        events.forEach(event -> {
            Long confirmedRequests = confirmedRequestsMap.getOrDefault(event.getId(), 0L);
            event.setConfirmedRequests(confirmedRequests);
        });
    }

    /**
     * Обогащение событий рейтингом из Analyzer
     */
    private void enrichEventsWithRatings(List<EventDtoFull> events) {
        if (events.isEmpty()) {
            return;
        }
        events.forEach(event -> {
            Double rating = getEventRating(event.getId());
            event.setRating(rating);
        });
    }

    /**
     * Обогащение отдельного события дополнительными данными
     */
    private void enrichEventWithAdditionalData(EventDtoFull event) {
        // Подтвержденные запросы
        Long confirmedRequests = requestService.countConfirmedRequests(event.getId());
        event.setConfirmedRequests(confirmedRequests);
        log.debug("Event {}: confirmedRequests = {}", event.getId(), confirmedRequests);

        // Рейтинг из Analyzer
        Double rating = getEventRating(event.getId());
        event.setRating(rating);
        log.debug("Event {}: rating = {}", event.getId(), rating);

        // Views больше не используется, оставляем 0 для обратной совместимости
        event.setViews(0L);
    }
}