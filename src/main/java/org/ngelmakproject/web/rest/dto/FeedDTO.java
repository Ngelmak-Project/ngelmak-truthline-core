package org.ngelmakproject.web.rest.dto;

public record FeedDTO(
    Long id,
    PostDTO post
) {
    public static FeedDTO from(Long id, PostDTO postDTO) {
        return new FeedDTO(
            id,
            postDTO
        );
    }
}
