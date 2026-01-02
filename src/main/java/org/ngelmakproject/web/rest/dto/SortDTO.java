package org.ngelmakproject.web.rest.dto;

import lombok.Data;

@Data
public class SortDTO {

    private String key;
    private String direction;

    public SortDTO(String key, String direction) {
        this.key = key;
        this.direction = direction;
    }
}
