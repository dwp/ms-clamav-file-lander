package uk.gov.dwp.health.clamav.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import uk.gov.dwp.health.clamav.utils.FileUtils;

import javax.validation.Validation;
import javax.validation.Validator;

@Configuration
public class AppConfig {

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  @Bean
  public Validator validator() {
    return Validation.buildDefaultValidatorFactory().getValidator();
  }

  @Bean
  public FileUtils fileUtils() {
    return new FileUtils();
  }

  @Bean
  @Autowired
  public RestTemplate restTemplate(
      final HttpRespStatusHandler errorHandler, final RestTemplateBuilder restTemplateBuilder) {
    return restTemplateBuilder.errorHandler(errorHandler).build();
  }
}
