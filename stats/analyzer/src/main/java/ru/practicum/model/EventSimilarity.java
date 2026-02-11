package ru.practicum.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "event_similarity")
@Getter
@Setter
@NoArgsConstructor
public class EventSimilarity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_a_id", nullable = false)
    private Integer eventAId;

    @Column(name = "event_b_id", nullable = false)
    private Integer eventBId;

    @Column(nullable = false)
    private Double score;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
        if (eventAId > eventBId) {
            int temp = eventAId;
            eventAId = eventBId;
            eventBId = temp;
        }
    }
}