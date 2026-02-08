package ru.practicum.comment.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.CommentService;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentDtoReq;
import ru.practicum.exception.CommentNotFoundException;
import ru.practicum.exception.EventNotFoundException;
import ru.practicum.exception.UserNotFoundException;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/comments")
@RequiredArgsConstructor
@Validated
public class CommentControllerPrivate {

    private final CommentService commentService;

    @PostMapping("/events/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId,
            @Valid @RequestBody CommentDto commentDto) throws UserNotFoundException, EventNotFoundException {
        return commentService.createComment(userId, eventId, commentDto);
    }

    @DeleteMapping("/{commentId}/events/{eventId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable @Positive Long userId,
                              @PathVariable @Positive Long eventId,
                              @Positive @PathVariable Long commentId) throws CommentNotFoundException {
        commentService.deleteComment(userId, eventId, commentId);
    }

    @PatchMapping("/{commentId}/events/{eventId}")
    public CommentDto updateComment(@PathVariable @Positive Long userId,
                                    @PathVariable @Positive Long eventId,
                                    @Positive @PathVariable Long commentId,
                                    @RequestBody @Validated CommentDtoReq commentDto) throws CommentNotFoundException {
        commentDto.setId(commentId);
        return commentService.updateComment(userId, eventId, commentDto);
    }

    @GetMapping
    public List<CommentDto> findUserComments(
            @PathVariable @Positive Long userId,
            @PageableDefault(page = 0, size = 10, sort = "created", direction = Sort.Direction.DESC) Pageable pageable) throws UserNotFoundException {
        return commentService.findUserComments(userId, pageable);
    }


}
