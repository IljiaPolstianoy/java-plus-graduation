package ru.practicum.comment;

import org.springframework.data.domain.Pageable;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentDtoReq;
import ru.practicum.comment.dto.CommentDtoShort;
import ru.practicum.comment.dto.CommentDtoStatus;
import ru.practicum.exception.CommentNotFoundException;
import ru.practicum.exception.EventNotFoundException;
import ru.practicum.exception.UserNotFoundException;

import java.util.List;

public interface CommentService {

    CommentDto createComment(Long userId, Long eventId, CommentDto commentDto) throws UserNotFoundException, EventNotFoundException;

    void deleteComment(Long userId, Long eventId, Long commentId) throws CommentNotFoundException;

    void deleteComment(Long eventId, Long commentId) throws CommentNotFoundException;

    void deleteComment(Long commentId) throws CommentNotFoundException;

    List<CommentDtoShort> findEventComments(Long eventId, Pageable pageable) throws EventNotFoundException;

    List<CommentDto> findUserComments(Long userId, Pageable pageable) throws UserNotFoundException;

    CommentDto findComment(Long commentId) throws CommentNotFoundException;

    CommentDto findComment(Long eventId, Long commentId) throws CommentNotFoundException;

    CommentDto updateComment(CommentDtoStatus commentDto) throws CommentNotFoundException;

    CommentDto updateComment(Long userId, Long eventId, CommentDtoReq commentDto) throws CommentNotFoundException;

}
