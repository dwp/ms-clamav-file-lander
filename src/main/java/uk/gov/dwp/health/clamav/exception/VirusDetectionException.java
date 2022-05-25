package uk.gov.dwp.health.clamav.exception;

public class VirusDetectionException extends RuntimeException {

  public VirusDetectionException(String msg) {
    super(msg);
  }
}
