package uk.gov.dwp.health.clamav.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class S3FileUploadExceptionTest {

  @Test
  void testS3FileUploadException() {
    S3FileUploadException cut = new S3FileUploadException("fail to upload file to s3");
    assertThat(cut.getMessage()).isEqualTo("fail to upload file to s3");
  }
}
