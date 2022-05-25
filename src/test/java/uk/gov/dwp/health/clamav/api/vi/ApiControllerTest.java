package uk.gov.dwp.health.clamav.api.vi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.dwp.health.clamav.api.v1.ApiController;
import uk.gov.dwp.health.clamav.exception.FailReadUploadFileException;
import uk.gov.dwp.health.clamav.exception.RequestValidationException;
import uk.gov.dwp.health.clamav.exception.VirusDetectionException;
import uk.gov.dwp.health.clamav.openapi.model.FileUploadResponse;
import uk.gov.dwp.health.clamav.openapi.model.ServiceRequestObject;
import uk.gov.dwp.health.clamav.service.impl.ClamAvClientImpl;
import uk.gov.dwp.health.clamav.service.impl.FileUploadImpl;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiControllerTest {

  @InjectMocks private ApiController cut;
  @Mock private FileUploadImpl fileSubmission;
  @Mock private Validator validator;
  @Mock private ClamAvClientImpl clamAvClient;

  @Test
  void testScanAndUploadSuccessful() {
    ServiceRequestObject meta = mock(ServiceRequestObject.class);
    MultipartFile file = mock(MultipartFile.class);
    FileUploadResponse response = new FileUploadResponse();
    when(fileSubmission.handleFileLanding(any(), any())).thenReturn(response);
    when(validator.validate(any(ServiceRequestObject.class))).thenReturn(Collections.emptySet());
    ResponseEntity<FileUploadResponse> actual = cut.scanAndUpload(meta, file);
    assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  @DisplayName("Test validate meta fail request validation exception thrown")
  void testValidateMetaFailRequestValidationExceptionThrown() {
    ServiceRequestObject meta = mock(ServiceRequestObject.class);
    MultipartFile file = mock(MultipartFile.class);
    Set<ConstraintViolation<ServiceRequestObject>> violations = spy(new HashSet<>());
    given(violations.size()).willReturn(1);
    when(validator.validate(any(ServiceRequestObject.class))).thenReturn(violations);
    assertThrows(RequestValidationException.class, () -> cut.scanAndUpload(meta, file));
  }

  @Test
  @DisplayName("Test valid Service Request Object All Empty, violation detected")
  void testValidServiceRequestObjectAllEmpty() {
    ServiceRequestObject meta = new ServiceRequestObject();
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    assertThat(validator.validate(meta)).isNotEmpty();
  }

  @Test
  @DisplayName("Test valid when id is empty string fail to validate")
  void testValidWhenIdIsEmptyStringFailToValidate() {
    ServiceRequestObject meta = new ServiceRequestObject();
    meta.setId("");
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    assertThat(validator.validate(meta)).isNotEmpty();
  }

  @Test
  @DisplayName("test scan virus only and 200 returned")
  void testScanVirusOnlyAnd200Returned() throws IOException {
    MultipartFile file = mock(MultipartFile.class);
    InputStream inputStream = mock(InputStream.class);
    when(file.getInputStream()).thenReturn(inputStream);
    when(clamAvClient.scanForVirus(any())).thenReturn(false);
    ResponseEntity<Void> actualResp = cut.scanOnly(file);
    assertThat(actualResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(clamAvClient).scanForVirus(inputStream);
  }

  @Test
  @DisplayName("test scan virus throws virus detection exception")
  void testScanVirusThrowsVirusDetectionException() throws IOException {
    MultipartFile file = mock(MultipartFile.class);
    InputStream inputStream = mock(InputStream.class);
    when(file.getInputStream()).thenReturn(inputStream);
    when(clamAvClient.scanForVirus(any())).thenReturn(true);
    assertThrows(VirusDetectionException.class, () -> cut.scanOnly(file));
    verify(clamAvClient).scanForVirus(inputStream);
  }

  @Test
  @DisplayName("test scan virus throws unable to read inputStream from upload")
  void testScanVirusThrowsUnableToReadInputStreamFromUpload() throws IOException {
    MultipartFile file = mock(MultipartFile.class);
    when(file.getInputStream()).thenThrow(IOException.class);
    assertThrows(FailReadUploadFileException.class, () -> cut.scanOnly(file));
    verify(clamAvClient, never()).scanForVirus(any(InputStream.class));
  }
}
