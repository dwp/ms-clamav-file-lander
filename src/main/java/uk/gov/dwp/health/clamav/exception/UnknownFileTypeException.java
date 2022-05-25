package uk.gov.dwp.health.clamav.exception;

public class UnknownFileTypeException extends RuntimeException {
  public UnknownFileTypeException(final String msg) {
    super(msg);
  }
}
