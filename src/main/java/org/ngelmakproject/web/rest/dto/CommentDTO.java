package org.ngelmakproject.web.rest.dto;

import java.time.Instant;

import org.ngelmakproject.domain.NkComment;

public record CommentDTO(
        Long id,
        String content,
        Instant at,
        AccountDTO author) {
    public static CommentDTO from(NkComment comment) {
        return new CommentDTO(
                comment.getId(),
                comment.getContent(),
                comment.getAt(),
                AccountDTO.from(comment.getAccount()));
    }
}
