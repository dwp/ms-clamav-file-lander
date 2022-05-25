package uk.gov.dwp.health.clamav.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FailReadUploadFileExceptionTest {

  @Test
  void testFailReadUploadedFileException() {
    var cut = new FailReadUploadFileException("fail to read uploaded file");
    assertThat(cut.getMessage()).isEqualTo("fail to read uploaded file");
  }
}
