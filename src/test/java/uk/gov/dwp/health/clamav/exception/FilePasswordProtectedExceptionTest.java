package uk.gov.dwp.health.clamav.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FilePasswordProtectedExceptionTest {

  @Test
  void testFilePasswordProtectedException() {
    var cut = new FilePasswordProtectedException("file is password protected");
    assertThat(cut.getMessage()).isEqualTo("file is password protected");
  }
}
