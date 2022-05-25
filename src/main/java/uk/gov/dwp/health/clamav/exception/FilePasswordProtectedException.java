package uk.gov.dwp.health.clamav.exception;

public class FilePasswordProtectedException extends RuntimeException {
  public FilePasswordProtectedException(final String msg) {
    super(msg);
  }
}
