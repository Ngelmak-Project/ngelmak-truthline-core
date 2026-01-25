package org.ngelmakproject.repository.projection;

import java.time.Instant;

public interface CommentProjection {
    Long getId();

    Instant getAt();

    Instant getLastUpdate();

    Instant getDeletedAt();

    String getContent();

    Long getPostId();

    Long getReplyToId();

    Long getAccountId();

    Long getFileId();

    Integer getReplyCount();
}
