package ru.practicum.comment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.comment.CommentStatus;

@Builder
@Setter
@Getter
public class CommentDtoStatus {
    @Null
    private Long id;

    @NotNull
    private CommentStatus status;

}
