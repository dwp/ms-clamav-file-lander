package uk.gov.dwp.health.clamav.config.properties;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.Validation;
import javax.validation.Validator;

import static org.assertj.core.api.Assertions.assertThat;

class S3ConfigPropertiesTest {

  private static Validator VALIDATOR;
  private S3ConfigProperties cut;

  @BeforeAll
  static void setupSpec() {
    VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
  }

  @BeforeEach
  void setup() {
    cut = new S3ConfigProperties();
  }

  @Test
  void testAwsS3PropertiesFailOneMissingRegion() {
    assertThat(VALIDATOR.validate(cut).size()).isEqualTo(2);
  }

  @Test
  void testAWSS3PropertiesOk() {
    cut.setAwsRegion("eu_west_2");
    cut.setBucket("mock_bucket");
    cut.setEndpointOverride("mock_endpoint_override");
    assertThat(VALIDATOR.validate(cut).size()).isZero();
    assertThat(cut.isPathStyleEnable()).isFalse();
    assertThat(cut.getAwsRegion()).isEqualTo("eu_west_2");
    assertThat(cut.getBucket()).isEqualTo("mock_bucket");
    assertThat(cut.getEndpointOverride()).isEqualTo("mock_endpoint_override");
  }
}
