package uk.gov.dwp.health.clamav.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FileWriterExceptionTest {

  @Test
  void testFileUploadException() {
    S3FileUploadException cut = new S3FileUploadException("file upload failed");
    assertThat(cut.getMessage()).isEqualTo("file upload failed");
  }
}
