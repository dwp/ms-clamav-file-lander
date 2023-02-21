package uk.gov.dwp.health.clamav.config.properties;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.regions.Region;

import javax.validation.Validator;

import static javax.validation.Validation.buildDefaultValidatorFactory;
import static org.assertj.core.api.Assertions.assertThat;

class S3ConfigPropertiesTest {

  private static Validator VALIDATOR;
  private S3ConfigProperties cut;

  @BeforeAll
  static void setupSpec() {
    try (var factory = buildDefaultValidatorFactory()) {
      VALIDATOR = factory.getValidator();
    }
  }

  @BeforeEach
  void setup() {
    cut = new S3ConfigProperties();
  }

  @Test
  void testAwsS3PropertiesFailOneMissingRegion() {
    assertThat(VALIDATOR.validate(cut).size()).isEqualTo(1);
  }

  @Test
  void testAWSS3PropertiesOk() {
    cut.setAwsRegion("eu-west-2");
    cut.setBucket("mock_bucket");
    cut.setEndpointOverride("mock_endpoint_override");
    assertThat(VALIDATOR.validate(cut).size()).isZero();
    assertThat(cut.isPathStyleEnable()).isFalse();
    assertThat(cut.getAwsRegion()).isEqualTo(Region.EU_WEST_2);
    assertThat(cut.getBucket()).isEqualTo("mock_bucket");
    assertThat(cut.getEndpointOverride()).isEqualTo("mock_endpoint_override");
  }
}
