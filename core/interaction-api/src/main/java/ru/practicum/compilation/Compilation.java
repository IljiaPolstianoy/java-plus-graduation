package ru.practicum.compilation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import ru.practicum.event.Event;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Table(name = "compilation")
@Builder
@Entity
@Getter
@Setter
@NamedEntityGraph(name = "compilation-with-events", attributeNodes = @NamedAttributeNode("events"))
public class Compilation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "pinned")
    private Boolean pinned;
    @Column(name = "title")
    private String title;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @BatchSize(size = 10)
    @JoinTable(
            name = "compilation_event",
            joinColumns = {@JoinColumn(name = "compilation_id")},
            inverseJoinColumns = {@JoinColumn(name = "event_id")}
    )
    @JsonIgnore
    private Set<Event> events;

    public void addEvent(Event event) {
        this.events.add(event);
        event.getCompilations().add(this);
    }

    public void removeEvent(Event event) {
        this.events.remove(event);
        event.getCompilations().remove(this);
    }
}
