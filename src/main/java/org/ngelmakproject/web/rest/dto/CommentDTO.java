package org.ngelmakproject.web.rest.dto;

import java.time.Instant;

import org.ngelmakproject.domain.NkComment;

public record CommentDTO(
        Long id,
        String content,
        Integer replyCount,
        Instant at,
        FileDTO file,
        AccountDTO account,
        CommentDTO replayto) {
    public static CommentDTO from(NkComment comment) {
        if (comment == null)
            return null;
        return new CommentDTO(
                comment.getId(),
                comment.getContent(),
                comment.getReplyCount(),
                comment.getAt(),
                FileDTO.from(comment.getFile()),
                AccountDTO.from(comment.getAccount()),
                CommentDTO.from(comment.getReplyTo()));
    }
}
