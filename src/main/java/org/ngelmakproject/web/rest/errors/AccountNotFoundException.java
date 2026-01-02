package org.ngelmakproject.web.rest.errors;

public class AccountNotFoundException extends ResourceNotFoundException {

  private static final long serialVersionUID = 1L;

  public AccountNotFoundException() {
    super("No account found.", "account", "accountNotFound");
  }
}