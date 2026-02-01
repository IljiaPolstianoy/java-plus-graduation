package ru.practicum.request.dto;

import lombok.*;
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
