package uk.gov.dwp.health.clamav.config.properties;

import com.amazonaws.regions.Regions;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "aws.encryption")
@Validated
public class CryptoConfigProperties {

  private String kmsOverride;

  @NotBlank(message = "KMS data key required")
  private String dataKey;

  private String region;

  public Regions getRegion() {
    return region == null || region.isBlank() ? Regions.EU_WEST_2 : Regions.fromName(region);
  }

}
