package uk.gov.dwp.health.clamav.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "clam.av")
@Configuration
@Getter
@Setter
public class NetConfigProperties {

  private String host = "localhost";
  private int port = 3310;
}
