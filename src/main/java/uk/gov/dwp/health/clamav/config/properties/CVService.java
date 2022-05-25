package uk.gov.dwp.health.clamav.config.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString
@Validated
public class CVService {

  @NotBlank(message = "service name identifier required")
  private String name;

  @NotBlank(message = "service uri required")
  private String uri;

  @NotBlank(message = "service endpoint required")
  private String endpoint;

  @Size(min = 1)
  private Set<String> types = new HashSet<>();
}
