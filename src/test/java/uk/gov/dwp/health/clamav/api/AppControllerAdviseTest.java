package uk.gov.dwp.health.clamav.api;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import uk.gov.dwp.health.clamav.constant.ResponseEnum;
import uk.gov.dwp.health.clamav.openapi.model.FailureResponse;
import uk.org.lidalia.slf4jext.Level;
import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AppControllerAdviseTest {

  private static AppControllerAdvise cut;
  private final TestLogger testLogger = TestLoggerFactory.getTestLogger(AppControllerAdvise.class);

  @BeforeAll
  static void setupSpec() {
    cut = new AppControllerAdvise();
  }

  @BeforeEach
  void setup() {
    testLogger.clearAll();
    ReflectionTestUtils.setField(cut, "log", testLogger);
  }

  @Test
  void testHandleAllUncaughtException() {
    Exception ex = mock(Exception.class);
    when(ex.getMessage()).thenReturn("ERROR ERROR");
    ResponseEntity<FailureResponse> actual = cut.handle500(ex);
    assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(actual.getBody().getMessage()).isEqualTo(ResponseEnum.UNKNOWN.message);
    assertThat(testLogger.getLoggingEvents())
        .containsExactly(new LoggingEvent(Level.ERROR, "Unknown error {}", "ERROR ERROR"));
  }

  @Test
  void testHandleBadMethodCallException() {
    var ex = mock(HttpRequestMethodNotSupportedException.class);
    when(ex.getMessage()).thenReturn("BAD METHOD");
    ResponseEntity<Void> actual = cut.handle405(ex);
    assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
    assertThat(actual.getBody()).isNull();
    assertThat(testLogger.getLoggingEvents())
        .containsExactly(
            new LoggingEvent(Level.WARN, "Request method not allowed {}", "BAD METHOD"));
  }

  @Test
  void testHandleRequestValidationException() {
    Exception ex = mock(Exception.class);
    when(ex.getMessage()).thenReturn("BAD REQUEST BODY");
    ResponseEntity<Void> actual = cut.handle400(ex);
    assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(actual.getBody()).isNull();
    assertThat(testLogger.getLoggingEvents())
        .containsExactly(
            new LoggingEvent(Level.WARN, "Request body validation failed {}", "BAD REQUEST BODY"));
  }

  @Test
  void testHandleVirusDetectionException() {
    ResponseEntity<FailureResponse> actual = cut.handleVirusDetectionException();
    assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.NOT_ACCEPTABLE);
    assertThat(actual.getBody().getMessage()).isEqualTo(ResponseEnum.VIRUS_DETECTED.message);
  }

  @Test
  void testHandleS3FailUploadException() {
    ResponseEntity<FailureResponse> actual = cut.handleS3FileUploadException();
    assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(actual.getBody().getMessage()).isEqualTo(ResponseEnum.S3_FAIL.message);
  }

  @Test
  void testHandleMultipartFileReadFailException() {
    ResponseEntity<FailureResponse> actual = cut.handleMultipartFailReadUploadException();
    assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(actual.getBody().getMessage()).isEqualTo(ResponseEnum.MULTIPART_FAIL.message);
  }

  @Test
  @DisplayName("Test conversion failure UnknownFileTypeException")
  void testConversionFailureUnknownFileTypeException() {
    ResponseEntity<FailureResponse> actual = cut.handleConversionException();
    assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(actual.getBody().getMessage()).isEqualTo(ResponseEnum.CONVERSION_FAIL.message);
  }

  @Test
  @DisplayName("Test conversion failure ConverterServiceException")
  void testConversionFailureConverterServiceException() {
    ResponseEntity<FailureResponse> actual = cut.handleConversionException();
    assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(actual.getBody().getMessage()).isEqualTo(ResponseEnum.CONVERSION_FAIL.message);
  }

  @Test
  @DisplayName("Test handle file is password protected exception thrown")
  void testHandleFileIsPasswordProtectedExceptionThrown() {
    ResponseEntity<FailureResponse> actual = cut.handleFilePasswordProtectedException();
    assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.NOT_ACCEPTABLE);
    assertThat(actual.getBody().getMessage()).isEqualTo(ResponseEnum.PASSWORD_PROTECTED.message);
  }
}
