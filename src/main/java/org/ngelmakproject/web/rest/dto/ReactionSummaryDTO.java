package org.ngelmakproject.web.rest.dto;

import java.util.Map;

public record ReactionSummaryDTO(
    Map<String, Integer> counts,
    String reactedByCurrentUser
) {}
