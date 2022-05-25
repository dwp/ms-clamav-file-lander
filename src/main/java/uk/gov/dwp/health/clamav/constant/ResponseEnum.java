package uk.gov.dwp.health.clamav.constant;

public enum ResponseEnum {
  SUCCESS(200, "ALL SUCCESS"),
  VIRUS_DETECTED(406, "VIRUS DETECTED"),
  PASSWORD_PROTECTED(406, "PASSWORD PROTECTED"),
  MULTIPART_FAIL(500, "MULTIPART FAIL"),
  S3_FAIL(500, "S3 FAIL"),
  CONVERSION_FAIL(500, "CONVERSION FAIL"),
  UNKNOWN(500, "UNKNOWN SERVER ERROR");

  public final String message;
  public final int status;

  ResponseEnum(int status, String message) {
    this.status = status;
    this.message = message;
  }
}
