package uk.gov.dwp.health.clamav.service.impl;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.dwp.health.clamav.config.properties.CVService;
import uk.gov.dwp.health.clamav.config.properties.CVServiceConfigProperties;
import uk.gov.dwp.health.clamav.exception.ConverterServiceException;
import uk.gov.dwp.health.clamav.http.pdfconvertor.PdfConvertRequest;
import uk.gov.dwp.health.clamav.http.pdfconvertor.PdfConvertResponse;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PdfConvertorClientServiceImplTest {

  private static HttpHeaders headers;
  private static String jsonRequest =
      "{\n"
          + "  \"id\": \"testId\",\n"
          + "  \"fileName\": \"fileName\",\n"
          + "  \"s3Ref\": \"tests3ref\",\n"
          + "  \"srcBucket\": \"pip-bucket\",\n"
          + "  \"destBucket\": \"pip-bucket\",\n"
          + "  \"scrEncryptEnabled\": true,\n"
          + "  \"destEncryptEnabled\": true\n"
          + "}";
  @InjectMocks PdfConvertorClientServiceImpl underTest;
  @Mock RestTemplate restTemplate;
  @Mock private CVServiceConfigProperties properties;

  @BeforeAll
  static void setupSpec() {
    headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
  }

  @Test
  void testPostConvertRequestSuccessfully() {
    CVService cvService = new CVService();
    cvService.setUri("http://img2pdf");
    cvService.setEndpoint("/v1/convert/s3");
    PdfConvertRequest request = mock(PdfConvertRequest.class);
    PdfConvertResponse pdfResponse = new PdfConvertResponse();
    pdfResponse.setSuccess(true);
    ResponseEntity<PdfConvertResponse> response =
        new ResponseEntity<>(pdfResponse, headers, HttpStatus.OK);
    when(properties.findServiceByMimeType("image/jpeg")).thenReturn(cvService);
    given(request.toJson()).willReturn(jsonRequest);

    HttpEntity<String> requestEntity = new HttpEntity<>(request.toJson(), headers);

    when(restTemplate.exchange(
            "http://img2pdf/v1/convert/s3",
            HttpMethod.POST,
            requestEntity,
            PdfConvertResponse.class))
        .thenReturn(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(underTest.postConvertRequest(request, "image/jpeg").isSuccess());
  }

  @Test
  void testPostConvertRequestThrowsConverterServiceException() {
    CVService cvService = new CVService();
    cvService.setUri("http://img2pdf");
    cvService.setEndpoint("/v1/convert/s3");
    PdfConvertRequest request = mock(PdfConvertRequest.class);
    PdfConvertResponse pdfResponse = new PdfConvertResponse();
    pdfResponse.setSuccess(false);
    pdfResponse.setMessage("failure of pdf converter");
    ResponseEntity<PdfConvertResponse> response =
        new ResponseEntity<>(pdfResponse, headers, HttpStatus.OK);
    when(properties.findServiceByMimeType("image/jpeg")).thenReturn(cvService);
    given(request.toJson()).willReturn(jsonRequest);

    HttpEntity<String> requestEntity = new HttpEntity<>(request.toJson(), headers);

    when(restTemplate.exchange(
            "http://img2pdf/v1/convert/s3",
            HttpMethod.POST,
            requestEntity,
            PdfConvertResponse.class))
        .thenReturn(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    var ex =
        assertThrows(
            ConverterServiceException.class,
            () -> underTest.postConvertRequest(request, "image/jpeg"));
    assertEquals("Error converting pdf failure of pdf converter", ex.getMessage());
  }

  @Test
  void testPostConvertRequestFailureonBadRequest() {
    CVService cvService = new CVService();
    cvService.setUri("http://img2pdf");
    cvService.setEndpoint("/v1/convert/s3");
    PdfConvertRequest request = mock(PdfConvertRequest.class);
    ResponseEntity<PdfConvertResponse> response =
        new ResponseEntity<>(null, headers, HttpStatus.BAD_REQUEST);
    when(properties.findServiceByMimeType("image/jpeg")).thenReturn(cvService);
    given(request.toJson())
        .willReturn(
            "{\n"
                + "  \"id\": \"testId\",\n"
                + "  \"fileName\": \"fileName\",\n"
                + "  \"s3Ref\": \"tests3ref\",\n"
                + "  \"srcBucket\": \"pip-bucket\",\n"
                + "  \"destBucket\": \"pip-bucket\",\n"
                + "  \"scrEncryptEnabled\": true,\n"
                + "  \"destEncryptEnabled\": true\n"
                + "}");

    HttpEntity<String> requestEntity = new HttpEntity<>(request.toJson(), headers);

    when(restTemplate.exchange(
            "http://img2pdf/v1/convert/s3",
            HttpMethod.POST,
            requestEntity,
            PdfConvertResponse.class))
        .thenReturn(response);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    var ex =
        assertThrows(
            ConverterServiceException.class,
            () -> underTest.postConvertRequest(request, "image/jpeg"));
    assertEquals("Error converting pdf 400", ex.getMessage());
  }

  @Test
  void testPostConvertRequestFailureonInternalServerError() {
    CVService cvService = new CVService();
    cvService.setUri("http://img2pdf");
    cvService.setEndpoint("/v1/convert/s3");
    PdfConvertRequest request = mock(PdfConvertRequest.class);
    ResponseEntity<PdfConvertResponse> response =
        new ResponseEntity<>(null, headers, HttpStatus.INTERNAL_SERVER_ERROR);
    when(properties.findServiceByMimeType("image/jpeg")).thenReturn(cvService);
    given(request.toJson())
        .willReturn(
            "{\n"
                + "  \"id\": \"testId\",\n"
                + "  \"fileName\": \"fileName\",\n"
                + "  \"s3Ref\": \"tests3ref\",\n"
                + "  \"srcBucket\": \"pip-bucket\",\n"
                + "  \"destBucket\": \"pip-bucket\",\n"
                + "  \"scrEncryptEnabled\": true,\n"
                + "  \"destEncryptEnabled\": true\n"
                + "}");

    HttpEntity<String> requestEntity = new HttpEntity<>(request.toJson(), headers);

    when(restTemplate.exchange(
            "http://img2pdf/v1/convert/s3",
            HttpMethod.POST,
            requestEntity,
            PdfConvertResponse.class))
        .thenReturn(response);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    var ex =
        assertThrows(
            ConverterServiceException.class,
            () -> underTest.postConvertRequest(request, "image/jpeg"));
    assertEquals("Error converting pdf 500", ex.getMessage());
  }
}
