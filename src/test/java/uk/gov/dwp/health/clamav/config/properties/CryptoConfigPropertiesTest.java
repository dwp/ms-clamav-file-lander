package uk.gov.dwp.health.clamav.config.properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CryptoConfigPropertiesTest {
  private CryptoConfigProperties cut;

  @BeforeEach
  void setup() {
    cut = new CryptoConfigProperties();
  }

  @Test
  void testCreateCryptoConfigProperties() {
    cut.setKmsOverride("mock_endpoint_override");
    assertThat(cut.getKmsOverride()).isEqualTo("mock_endpoint_override");
  }
}
