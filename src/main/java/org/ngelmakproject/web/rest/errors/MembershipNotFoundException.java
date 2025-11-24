package org.ngelmakproject.web.rest.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;

import tech.jhipster.web.rest.errors.ProblemDetailWithCause.ProblemDetailWithCauseBuilder;


public class MembershipNotFoundException extends ErrorResponseException {

    private static final long serialVersionUID = 1L;

    public MembershipNotFoundException() {
        super(
            HttpStatus.NOT_FOUND,
            ProblemDetailWithCauseBuilder.instance()
                .withStatus(HttpStatus.NOT_FOUND.value())
                .withTitle("NkMembership not found.")
                .withProperty("message", "error.membershipNotFound")
                .withProperty("params", "membership")
                .build(),
            null
        );
    }
}
