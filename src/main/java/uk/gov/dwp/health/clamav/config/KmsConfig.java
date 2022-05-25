package uk.gov.dwp.health.clamav.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.dwp.health.clamav.config.properties.CryptoConfigProperties;
import uk.gov.dwp.health.clamav.exception.CryptoConfigException;
import uk.gov.dwp.health.crypto.CryptoConfig;
import uk.gov.dwp.health.crypto.CryptoDataManager;
import uk.gov.dwp.health.crypto.exception.CryptoException;

@Slf4j
@Configuration
public class KmsConfig {

  @SneakyThrows
  @Bean
  public CryptoConfig cryptoConfig(final CryptoConfigProperties properties) {
    CryptoConfig config = new CryptoConfig(properties.getDataKey());
    if (properties.getKmsOverride() != null && !properties.getKmsOverride().isBlank()) {
      config.setKmsEndpointOverride(properties.getKmsOverride());
    }
    return config;
  }

  @SneakyThrows
  @Autowired
  @Bean
  public CryptoDataManager cryptoDataManager(CryptoConfig configuration) {
    try {
      return new CryptoDataManager(configuration);
    } catch (CryptoException ex) {
      final String msg = String.format("kms crypto config error %s", ex.getMessage());
      log.error(msg);
      throw new CryptoConfigException(msg);
    }
  }
}
