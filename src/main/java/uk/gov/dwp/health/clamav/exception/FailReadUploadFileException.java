package uk.gov.dwp.health.clamav.exception;

public class FailReadUploadFileException extends RuntimeException {

  public FailReadUploadFileException(final String message) {
    super(message);
  }
}
