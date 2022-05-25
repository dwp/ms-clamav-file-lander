package uk.gov.dwp.health.clamav.config.properties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import uk.gov.dwp.health.clamav.exception.UnknownFileTypeException;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Slf4j
@ConfigurationProperties(prefix = "uk.gov.dwp.health.converter")
@Component
@Validated
public class CVServiceConfigProperties {

  @Valid private List<CVService> services = new ArrayList<>();

  public CVService findServiceByMimeType(final String mime) {
    if (mime == null || mime.isBlank()) {
      final String msg = "MIME type must not be null or blank";
      log.error(msg);
      throw new IllegalArgumentException(msg);
    }
    return services.stream()
        .filter(s -> s.getTypes().contains(mime))
        .findAny()
        .orElseThrow(
            () -> {
              final String msg = String.format("%s type is not unknown to any converter", mime);
              log.warn(msg);
              return new UnknownFileTypeException(msg);
            });
  }
}
