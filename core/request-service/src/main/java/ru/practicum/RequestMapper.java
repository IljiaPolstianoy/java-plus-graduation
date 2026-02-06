package ru.practicum;

import org.springframework.stereotype.Component;
import ru.practicum.event.Event;
import ru.practicum.request.Request;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.user.User;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RequestMapper {

    public RequestDto toDto(Request request) {
        if (request == null) {
            return null;
        }

        return RequestDto.builder()
                .id(request.getId())
                .created(request.getCreated())
                .event(request.getEventId())
                .requester(request.getRequesterId())
                .status(request.getStatus())
                .build();
    }

    public List<RequestDto> toDtoList(List<Request> requests) {
        if (requests == null) {
            return null;
        }

        return requests.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ParticipationRequestDto toParticipationDto(Request request) {
        if (request == null) {
            return null;
        }

        return ParticipationRequestDto.builder()
                .id(request.getId())
                .created(request.getCreated())
                .event(request.getEventId())
                .requester(request.getRequesterId())
                .status(request.getStatus())
                .build();
    }

    public List<ParticipationRequestDto> toParticipationDtoList(List<Request> requests) {
        if (requests == null) {
            return null;
        }

        return requests.stream()
                .map(this::toParticipationDto)
                .collect(Collectors.toList());
    }

    public Long mapEventToId(Event event) {
        return event != null ? event.getId() : null;
    }

    public Long mapUserToId(User user) {
        return user != null ? user.getId() : null;
    }

    public Event mapIdToEvent(Long eventId) {
        if (eventId == null) {
            return null;
        }
        return Event.builder().id(eventId).build();
    }

    public User mapIdToUser(Long userId) {
        if (userId == null) {
            return null;
        }
        return User.builder().id(userId).build();
    }
}