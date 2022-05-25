package uk.gov.dwp.health.clamav.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClamAvServiceExceptionTest {

  @Test
  void testCreateClamAvServiceException() {
    ClamAvServiceException cut = new ClamAvServiceException("Clam av service not available");
    assertThat(cut.getMessage()).isEqualTo("Clam av service not available");
  }
}
