package uk.gov.dwp.health.clamav.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CryptoConfigExceptionTest {

  @Test
  void testCreateCryptoConfigException() {
    CryptoConfigException cut = new CryptoConfigException("crypto config fail");
    assertThat(cut.getMessage()).isEqualTo("crypto config fail");
  }
}
