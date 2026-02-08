package org.ngelmakproject.web.rest.dto;

import java.time.Instant;

import org.ngelmakproject.domain.Comment;

public record CommentDTO(
        Long id,
        String content,
        Integer replyCount,
        Instant at,
        FileDTO file,
        AccountDTO account,
        CommentDTO replayto) {
    public static CommentDTO from(Comment c) {
        if (c == null)
            return null;
        return new CommentDTO(
                c.getId(),
                c.getContent(),
                c.getReplyCount(),
                c.getAt(),
                FileDTO.from(c.getFile()),
                AccountDTO.from(c.getAccount()),
                CommentDTO.from(c.getReplyTo()));
    }
}
