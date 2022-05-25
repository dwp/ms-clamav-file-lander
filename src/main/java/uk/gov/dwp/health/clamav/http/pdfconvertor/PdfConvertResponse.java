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
public class PdfConvertResponse {

  @JsonIgnore private final ObjectMapper mapper = new ObjectMapper();
  private String s3Ref;
  private String bucketName;
  private boolean success;
  private String message;
  private int fileSizeKb;

  public String toJson() {
    try {
      return mapper.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      log.error("Unable to map to json string {}", e.getMessage());
    }
    return "{}";
  }
}
