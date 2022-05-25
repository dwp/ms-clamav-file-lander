package uk.gov.dwp.health.clamav.exception;

public class RequestValidationException extends RuntimeException {

  public RequestValidationException(final String msg) {
    super(msg);
  }
}
