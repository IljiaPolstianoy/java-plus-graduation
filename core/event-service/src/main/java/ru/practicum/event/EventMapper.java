package ru.practicum.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.category.Category;
import ru.practicum.category.CategoryDto;
import ru.practicum.event.dto.EventDto;
import ru.practicum.event.dto.EventDtoFull;
import ru.practicum.user.dto.UserDto;
import ru.practicum.exception.LocationNotFound;
import ru.practicum.feign.CategoryFeignClient;
import ru.practicum.feign.UserFeignClient;
import ru.practicum.location.Location;
import ru.practicum.location.LocationDto;
import ru.practicum.location.LocationRepository;
import ru.practicum.user.User;

@Component
@RequiredArgsConstructor
public class EventMapper {

    private final CategoryFeignClient categoryRepository;
    private final UserFeignClient userRepository;
    private final LocationRepository locationRepository;

    // EventDto
    public EventDto toEventDto(Event event) {
        return EventDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(event.getCategoryId())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(event.getInitiatorId())
                .location(mapLocationIdToDto(event.getLocationId()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .requestModeration(event.getRequestModeration())
                .title(event.getTitle())
                .views(event.getViews())
                .build();
    }

    // EventDtoFull (полные объекты)
    public EventDtoFull toEventFullDto(Event event) {
        return EventDtoFull.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(mapCategoryIdToDto(event.getCategoryId()))
                .confirmedRequests(event.getConfirmedRequests())
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(mapUserIdToUserDto(event.getInitiatorId()))
                .location(mapLocationIdToDto(event.getLocationId()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .title(event.getTitle())
                .views(event.getViews())
                .build();
    }

    // Event из EventDto
    public Event toEvent(EventDto eventDto) {
        Event event = new Event();
        event.setId(eventDto.getId());
        event.setAnnotation(eventDto.getAnnotation());
        event.setDescription(eventDto.getDescription());
        event.setEventDate(eventDto.getEventDate());
        event.setPaid(eventDto.getPaid());
        event.setParticipantLimit(eventDto.getParticipantLimit());
        event.setRequestModeration(eventDto.getRequestModeration());
        event.setTitle(eventDto.getTitle());
        // Поля state, createdOn, publishedOn, confirmedRequests, views — игнорируются (set в сервисе)
        return event;
    }

    // Обновление существующего Event из EventDto
    public void updateEventFromDto(EventDto eventDto, Event event) {
        if (eventDto.getAnnotation() != null) {
            event.setAnnotation(eventDto.getAnnotation());
        }
        if (eventDto.getDescription() != null) {
            event.setDescription(eventDto.getDescription());
        }
        if (eventDto.getEventDate() != null) {
            event.setEventDate(eventDto.getEventDate());
        }
        if (eventDto.getPaid() != null) {
            event.setPaid(eventDto.getPaid());
        }
        if (eventDto.getParticipantLimit() != null) {
            event.setParticipantLimit(eventDto.getParticipantLimit());
        }
        if (eventDto.getRequestModeration() != null) {
            event.setRequestModeration(eventDto.getRequestModeration());
        }
        if (eventDto.getTitle() != null) {
            event.setTitle(eventDto.getTitle());
        }
        // Остальные поля (state, createdOn и др.) не обновляются
    }

    // Event из EventDtoFull (полное преобразование)
    public Event toEventFromFullDto(EventDtoFull eventDtoFull) {
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


    // Методы для получения объектов по ID
    protected CategoryDto mapCategoryIdToDto(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        Category category = categoryRepository.findById(categoryId);
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    protected UserDto mapUserIdToUserDto(Long userId) {
        if (userId == null) {
            return null;
        }
        User user = userRepository.findById(userId);
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    protected LocationDto mapLocationIdToDto(Long locationId) {
        if (locationId == null) {
            return null;
        }
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new LocationNotFound("Location with id " + locationId + " not found"));
        return LocationDto.builder()
                .id(location.getId())
                .lat(location.getLat())
                .lon(location.getLon())
                .build();
    }

    protected Long mapLocationDtoToId(LocationDto locationDto) {
        if (locationDto == null) {
            return null;
        }

        // Если locationDto уже имеет ID, используем существующий
        if (locationDto.getId() != null) {
            return locationDto.getId();
        }

        // Если ID нет, сохраняем новую локацию
        Location location = Location.builder()
                .lat(locationDto.getLat())
                .lon(locationDto.getLon())
                .build();
        Location savedLocation = locationRepository.save(location);
        return savedLocation.getId();
    }

    // Валидация и установка категории
    protected void validateAndSetCategory(Event event, EventDto eventDto) {
        if (eventDto.getCategory() != null) {
            categoryRepository.findById(eventDto.getCategory()); // проверка существования
        }
    }

    // Валидация и установка пользователя
    protected void validateAndSetUser(Event event, EventDto eventDto) {
        if (eventDto.getInitiator() != null) {
            userRepository.findById(eventDto.getInitiator()); // проверка существования
        }
    }
}
