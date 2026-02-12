package ru.practicum.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.ActionType;
import ru.practicum.model.ActionWeight;

import java.util.Optional;

public interface ActionWeightRepository extends JpaRepository<ActionWeight, ActionType> {
    Optional<ActionWeight> findByActionType(ActionType actionType);
}