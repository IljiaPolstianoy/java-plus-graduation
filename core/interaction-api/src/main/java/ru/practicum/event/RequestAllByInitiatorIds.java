package ru.practicum.event;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class RequestAllByInitiatorIds {

    private final List<Long> userId;

    @NotNull
    private final Pageable pageable;
}
