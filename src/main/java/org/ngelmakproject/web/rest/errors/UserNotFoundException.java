package org.ngelmakproject.web.rest.errors;

public class UserNotFoundException extends ResourceNotFoundException {

    private static final long serialVersionUID = 1L;

    public UserNotFoundException() {
        super("NkUser not found.", "userManagement", "userNotFound");
    }
}
