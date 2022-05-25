package uk.gov.dwp.health.clamav.http.pdfconvertor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PdfConvertRequest {

  @JsonIgnore private final ObjectMapper mapper = new ObjectMapper();
  private String id;
  private String fileName;
  private String s3Ref;
  private String srcBucket;
  private String destBucket;
  private boolean scrEncryptEnabled = true;
  private boolean destEncryptEnabled = true;

  public String toJson() {
    try {
      return mapper.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      log.error("Unable to map to json string {}", e.getMessage());
    }
    return "{}";
  }
}
