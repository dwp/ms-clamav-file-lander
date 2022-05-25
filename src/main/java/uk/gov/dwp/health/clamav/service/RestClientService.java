package uk.gov.dwp.health.clamav.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.dwp.health.clamav.http.pdfconvertor.PdfConvertResponse;

import java.util.Collections;

public interface RestClientService<T> {

  default ResponseEntity<PdfConvertResponse> postRequest(
      final RestTemplate template, final String url, final String jsonPayload) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    HttpEntity<String> requestEntity = new HttpEntity<>(jsonPayload, headers);
    return template.exchange(url, HttpMethod.POST, requestEntity, PdfConvertResponse.class);
  }

  PdfConvertResponse postConvertRequest(T request, String contentType);
}
