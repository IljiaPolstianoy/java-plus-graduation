package ru.practicum.subscription.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import ru.practicum.subscription.model.Subscription;
import ru.practicum.subscription.model.SubscriptionDtoProjection;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<SubscriptionDtoProjection> findByUserId(Long id);

    @Modifying
    void deleteByUserIdAndSubscriptionId(Long userId, Long subscriptionId);
}