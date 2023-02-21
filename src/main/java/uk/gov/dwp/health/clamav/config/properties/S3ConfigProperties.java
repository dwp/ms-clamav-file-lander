package uk.gov.dwp.health.clamav.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import software.amazon.awssdk.regions.Region;

import javax.validation.constraints.NotBlank;

@ConfigurationProperties(prefix = "aws.s3")
@Configuration
@Validated
@Getter
@Setter
public class S3ConfigProperties {

  @NotBlank(message = "AWS region required")
  private String awsRegion = "eu-west-2";

  @NotBlank(message = "S3 bucket name required")
  private String bucket;

  private String endpointOverride;

  private boolean pathStyleEnable;

  private boolean encryptEnable;

  public Region getAwsRegion() {
    return Region.of(this.awsRegion);
  }
}
