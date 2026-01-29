package ru.practicum.mainservice.comment.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.exception.CommentNotFoundException;
import ru.practicum.mainservice.comment.CommentService;
import ru.practicum.mainservice.comment.dto.CommentDto;
import ru.practicum.mainservice.comment.dto.CommentDtoStatus;

@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
@Validated
public class CommentControllerAdmin {

    private final CommentService commentService;

    @GetMapping("/{commentId}")
    public CommentDto findComment(@PathVariable @Positive Long commentId) throws CommentNotFoundException {
        return commentService.findComment(commentId);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@Positive @PathVariable Long commentId) throws CommentNotFoundException {
        commentService.deleteComment(commentId);
    }

    // публикацция или сокрытие комментария админом
    @PatchMapping("/{commentId}")
    public CommentDto updateComment(@Positive @PathVariable Long commentId, @RequestBody  @Validated CommentDtoStatus commentDto) throws CommentNotFoundException {
        commentDto.setId(commentId);
        return commentService.updateComment(commentDto);
    }

}
