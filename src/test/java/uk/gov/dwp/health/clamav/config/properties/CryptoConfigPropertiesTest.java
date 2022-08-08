package uk.gov.dwp.health.clamav.config.properties;

import com.amazonaws.regions.Regions;
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

  @Test
  void get_region_should_return_default_eu_west_2_regions() {
    assertThat(cut.getRegion()).isEqualTo(Regions.EU_WEST_2);
  }

  @Test
  void get_region_should_return_default_us_east_1_regions() {
    cut.setRegion("us-east-1");
    assertThat(cut.getRegion()).isEqualTo(Regions.US_EAST_1);
  }
}
