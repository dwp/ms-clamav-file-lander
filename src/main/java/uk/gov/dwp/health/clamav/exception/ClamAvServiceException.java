package uk.gov.dwp.health.clamav.exception;

public class ClamAvServiceException extends RuntimeException {

  public ClamAvServiceException(final String msg) {
    super(msg);
  }
}
