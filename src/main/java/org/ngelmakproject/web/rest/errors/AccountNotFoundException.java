package org.ngelmakproject.web.rest.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;

import tech.jhipster.web.rest.errors.ProblemDetailWithCause.ProblemDetailWithCauseBuilder;


public class AccountNotFoundException extends ErrorResponseException {

    private static final long serialVersionUID = 1L;

    public AccountNotFoundException() {
        super(
            HttpStatus.NOT_FOUND,
            ProblemDetailWithCauseBuilder.instance()
                .withStatus(HttpStatus.NOT_FOUND.value())
                .withTitle("Account not found.")
                .withProperty("message", "error.accountNotFound")
                .withProperty("params", "nk_account")
                .build(),
            null
        );
    }
}
