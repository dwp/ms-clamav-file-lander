package uk.gov.dwp.health.clamav.config.properties;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NetConfigPropertiesTest {

  @Test
  void createNetConfigPropsWithDefaultValues() {
    NetConfigProperties cut = new NetConfigProperties();
    assertThat(cut.getHost()).isEqualTo("localhost");
    assertThat(cut.getPort()).isEqualTo(3310);
  }
}
