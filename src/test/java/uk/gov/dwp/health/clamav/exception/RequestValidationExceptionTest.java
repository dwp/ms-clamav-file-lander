package uk.gov.dwp.health.clamav.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RequestValidationExceptionTest {

  @Test
  @DisplayName("Test create request validation exception")
  void testCreateRequestValidationException() {
    var actual = new RequestValidationException("fail to validate");
    assertThat(actual.getMessage()).isEqualTo("fail to validate");
  }
}
