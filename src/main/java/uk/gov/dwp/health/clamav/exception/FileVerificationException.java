package uk.gov.dwp.health.clamav.exception;

public class FileVerificationException extends RuntimeException {

  public FileVerificationException(final String msg) {
    super(msg);
  }
}
