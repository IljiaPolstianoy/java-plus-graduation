package ru.practicum.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import ru.practicum.compilation.Compilation;
import ru.practicum.enums.EventState;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "event")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String annotation;

    @Column(name = "category_id")
    private Long categoryId;

    // Счетчик не обновляется в бд, считается по запросам на участие
    private Long confirmedRequests;

    @Column(name = "created_on")
    private LocalDateTime createdOn;

    private String description;

    private LocalDateTime eventDate;

    @Column(name = "initiator_id")
    private Long initiatorId;

    @Column(name = "location_id")
    private Long locationId;

    private Boolean paid;

    @Column(name = "participant_limit")
    private Integer participantLimit;

    @Column(name = "published_on")
    private LocalDateTime publishedOn;

    private Boolean requestModeration;

    @Enumerated(EnumType.STRING)
    private EventState state;

    private String title;

    // Не хранится в бд, получается через клиента из сервера статистики
    private Long views;

    @ManyToMany(mappedBy = "events")
    @JsonIgnore
    private Set<Compilation> compilations;
}
