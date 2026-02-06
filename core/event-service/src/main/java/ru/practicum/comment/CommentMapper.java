package ru.practicum.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentDtoShort;
import ru.practicum.event.Event;
import ru.practicum.event.EventRepository;
import ru.practicum.feign.UserRepository;
import ru.practicum.user.User;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CommentMapper {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public Comment toEntity(CommentDto commentDto) {
        if (commentDto == null) {
            return null;
        }

        return Comment.builder()
                .id(commentDto.getId())
                .text(commentDto.getText())
                .eventId(commentDto.getEventId())
                .authorId(commentDto.getAuthorId())
                .status(commentDto.getStatus())
                .created(commentDto.getCreated())
                .build();
    }

    public CommentDto toDto(Comment entity) {
        if (entity == null) {
            return null;
        }

        return CommentDto.builder()
                .id(entity.getId())
                .text(entity.getText())
                .eventId(entity.getEventId())
                .authorId(entity.getAuthorId())
                .status(entity.getStatus())
                .created(entity.getCreated())
                .build();
    }

    public CommentDtoShort toDtoShort(Comment entity) {
        if (entity == null) {
            return null;
        }

        // Получаем автора из репозитория
        String authorName = null;
        if (entity.getAuthorId() != null) {
            User author = userRepository.findById(entity.getAuthorId());
            if (author != null) {
                authorName = author.getName();
            }
        }

        // Получаем аннотацию события из репозитория
        String eventAnnotation = null;
        if (entity.getEventId() != null) {
            Event event = eventRepository.findById(entity.getEventId()).orElse(null);
            if (event != null) {
                eventAnnotation = event.getAnnotation();
            }
        }

        return CommentDtoShort.builder()
                .id(entity.getId())
                .text(entity.getText())
                .authorName(authorName)
                .created(entity.getCreated())
                .eventIAnnotation(eventAnnotation)
                .build();
    }

    // Дополнительный метод для создания комментария с автоматической установкой даты
    public Comment createComment(CommentDto commentDto, Long authorId, Long eventId) {
        LocalDateTime now = LocalDateTime.now();

        return Comment.builder()
                .text(commentDto.getText())
                .eventId(eventId)
                .authorId(authorId)
                .status(commentDto.getStatus() != null ? commentDto.getStatus() : CommentStatus.PENDING)
                .created(now)
                .build();
    }

    // Метод для обновления существующего комментария
    public void updateComment(Comment existingComment, CommentDto updateDto) {
        if (updateDto.getText() != null && !updateDto.getText().isBlank()) {
            existingComment.setText(updateDto.getText());
        }

        if (updateDto.getStatus() != null) {
            existingComment.setStatus(updateDto.getStatus());
        }
    }
}