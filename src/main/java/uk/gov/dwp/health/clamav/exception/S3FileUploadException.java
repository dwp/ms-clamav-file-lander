package uk.gov.dwp.health.clamav.exception;

public class S3FileUploadException extends RuntimeException {

  public S3FileUploadException(final String msg) {
    super(msg);
  }
}
