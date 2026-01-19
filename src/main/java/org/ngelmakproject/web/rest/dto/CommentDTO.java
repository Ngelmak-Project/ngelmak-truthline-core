package org.ngelmakproject.web.rest.dto;

import java.time.Instant;

public record CommentDTO(
    Long id,
    String content,
    Instant at,
    AccountDTO author
) {}
