package uk.gov.dwp.health.clamav.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UnknownFileTypeExceptionTest {

  @Test
  @DisplayName("Test unknownFileTypeException")
  void testUnknownFileTypeException() {
    var cut = new UnknownFileTypeException("mime type unknown");
    assertEquals("mime type unknown", cut.getMessage());
  }
}
