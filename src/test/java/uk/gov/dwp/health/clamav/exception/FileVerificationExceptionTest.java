package uk.gov.dwp.health.clamav.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FileVerificationExceptionTest {

  @Test
  void testFileVerificationException() {
    FileVerificationException cut = new FileVerificationException("checksum failed");
    assertThat(cut.getMessage()).isEqualTo("checksum failed");
  }
}
