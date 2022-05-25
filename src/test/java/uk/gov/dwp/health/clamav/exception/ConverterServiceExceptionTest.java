package uk.gov.dwp.health.clamav.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConverterServiceExceptionTest {

  @Test
  void testConverterServiceException() {
    var cut = new ConverterServiceException("converter service exception");
    assertThat(cut.getMessage()).isEqualTo("converter service exception");
  }
}
