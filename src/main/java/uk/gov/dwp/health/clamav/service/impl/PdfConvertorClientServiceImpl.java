package uk.gov.dwp.health.clamav.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.dwp.health.clamav.config.properties.CVService;
import uk.gov.dwp.health.clamav.config.properties.CVServiceConfigProperties;
import uk.gov.dwp.health.clamav.exception.ConverterServiceException;
import uk.gov.dwp.health.clamav.http.pdfconvertor.PdfConvertRequest;
import uk.gov.dwp.health.clamav.http.pdfconvertor.PdfConvertResponse;
import uk.gov.dwp.health.clamav.service.RestClientService;

@Slf4j
@Service
public class PdfConvertorClientServiceImpl implements RestClientService<PdfConvertRequest> {

  private final CVServiceConfigProperties properties;
  private final RestTemplate restTemplate;

  public PdfConvertorClientServiceImpl(
      final RestTemplate restTemplate, final CVServiceConfigProperties properties) {
    this.restTemplate = restTemplate;
    this.properties = properties;
  }

  @Override
  public PdfConvertResponse postConvertRequest(
      final PdfConvertRequest request, final String contentType) {
    var cvService = properties.findServiceByMimeType(contentType);
    log.info("File conversion to be send to service {}", cvService.getName());
    ResponseEntity<PdfConvertResponse> resp =
        postRequest(restTemplate, cvService.getUri() + cvService.getEndpoint(), request.toJson());
    log.info("File conversion completed - response received");
    return handleConversionResponse(resp);
  }

  private PdfConvertResponse handleConversionResponse(
      final ResponseEntity<PdfConvertResponse> resp) {
    if (resp.getStatusCode() == HttpStatus.OK && resp.getBody() != null) {
      log.info("pdf convertor request OK");
      if (resp.getBody().isSuccess()) {
        return resp.getBody();
      } else {
        final String msg = String.format("Error converting pdf %s", resp.getBody().getMessage());
        log.error(msg);
        throw new ConverterServiceException(msg);
      }
    } else {
      final String msg = String.format("Error converting pdf %d", resp.getStatusCode().value());
      log.error(msg);
      throw new ConverterServiceException(msg);
    }
  }
}
