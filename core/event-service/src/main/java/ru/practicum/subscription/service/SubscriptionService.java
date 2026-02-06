package ru.practicum.subscription.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.practicum.event.Event;
import ru.practicum.exception.UserNotFoundException;
import ru.practicum.subscription.model.Subscription;
import ru.practicum.subscription.model.SubscriptionDtoProjection;

import java.util.List;

public interface SubscriptionService {

    Subscription create(Long subscription, Long user) throws UserNotFoundException;

    void delete(Long userId, Long subscriptionId);

    List<SubscriptionDtoProjection> getAllSubscriptions(Long id);

    Page<Event> getAllEvents(Long userId, Pageable pageable);
}