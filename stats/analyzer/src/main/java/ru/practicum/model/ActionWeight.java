package ru.practicum.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "action_weight")
@Getter
@Setter
@NoArgsConstructor
public class ActionWeight {

    @Id
    @Column(name = "action_type", length = 20)
    @Enumerated(EnumType.STRING)
    private ActionType actionType;

    @Column(nullable = false)
    private Double weight;
}