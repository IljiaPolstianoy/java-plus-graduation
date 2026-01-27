package ru.practicum.request.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.request.RequestStatus;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RequestDto {
    private Long id;

    private LocalDateTime created;

    private Long event;
    private Long requester;

    private RequestStatus status;

}
