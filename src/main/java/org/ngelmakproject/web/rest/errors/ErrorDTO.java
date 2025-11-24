package org.ngelmakproject.web.rest.errors;

import java.io.Serializable;

import org.springframework.http.HttpStatus;

import lombok.Data;

@Data
public class ErrorDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String objectName;

    private final HttpStatus status;

    private final String message;

    public ErrorDTO(String dto, HttpStatus status, String message) {
        this.objectName = dto;
        this.status = status;
        this.message = message;
    }
}
