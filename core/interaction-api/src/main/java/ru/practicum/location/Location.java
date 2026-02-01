package ru.practicum.location;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "location")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(precision = 9, scale = 6, nullable = false)
    private BigDecimal lat;

    @Column(precision = 9, scale = 6, nullable = false)
    private BigDecimal lon;
}
