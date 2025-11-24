package org.ngelmakproject.web.rest.errors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(BadRequestAlertException.class)
  public ResponseEntity<Object> handleResourceNotFoundException(BadRequestAlertException ex) {
    return new ResponseEntity<>(ex.getMessage(), ex.getStatusCode());
  }
}
