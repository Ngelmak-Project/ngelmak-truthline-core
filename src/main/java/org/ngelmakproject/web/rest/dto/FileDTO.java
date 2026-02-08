package org.ngelmakproject.web.rest.dto;

import org.ngelmakproject.domain.File;

public record FileDTO(
        Long id,
        Long size,
        String url,
        String type,
        String filename,
        Integer duration) {
    public static FileDTO from(File f) {
        if (f == null)
            return null;
        return new FileDTO(
                f.getId(),
                f.getSize(),
                f.getUrl(),
                f.getType(),
                f.getFilename(),
                f.getDuration());
    }
}
