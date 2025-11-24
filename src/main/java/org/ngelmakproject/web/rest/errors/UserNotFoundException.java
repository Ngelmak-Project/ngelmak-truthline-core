package org.ngelmakproject.web.rest.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;

import tech.jhipster.web.rest.errors.ProblemDetailWithCause.ProblemDetailWithCauseBuilder;


public class UserNotFoundException extends ErrorResponseException {

    private static final long serialVersionUID = 1L;

    public UserNotFoundException() {
        super(
            HttpStatus.NOT_FOUND,
            ProblemDetailWithCauseBuilder.instance()
                .withStatus(HttpStatus.NOT_FOUND.value())
                .withTitle("User not found.")
                .withProperty("message", "error.userNotFound")
                .withProperty("params", "user")
                .build(),
            null
        );
    }
}
