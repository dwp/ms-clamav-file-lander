package uk.gov.dwp.health.clamav.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VirusDetectionExceptionTest {

  @Test
  void testCreateVirusDetectionException() {
    var cut = new VirusDetectionException("virus detected");
    assertThat(cut.getMessage()).isEqualTo("virus detected");
  }
}
