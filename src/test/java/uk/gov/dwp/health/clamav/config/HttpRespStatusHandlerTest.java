package uk.gov.dwp.health.clamav.config;

import com.amazonaws.services.kms.model.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.dwp.health.clamav.exception.ConverterServiceException;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HttpRespStatusHandlerTest {

  private TestLogger testLogger = TestLoggerFactory.getTestLogger(HttpRespStatusHandler.class);
  private HttpRespStatusHandler cut;

  private static Stream<Arguments> testCase() {
    return Stream.of(
        Arguments.of(HttpStatus.BAD_REQUEST, true),
        Arguments.of(HttpStatus.INTERNAL_SERVER_ERROR, true));
  }

  @BeforeEach
  void setup() {
    cut = new HttpRespStatusHandler();
    testLogger.clearAll();
  }

  @ParameterizedTest
  @MethodSource(value = "testCase")
  void testHasErrorWithAllowedStatusCode(HttpStatus respStatus, boolean expect) throws IOException {
    HttpRespStatusHandler underTest = new HttpRespStatusHandler();
    ClientHttpResponse response = mock(ClientHttpResponse.class);
    when(response.getStatusCode()).thenReturn(respStatus);
    assertThat(underTest.hasError(response)).isEqualTo(expect);
  }

  @Test
  void testHandleErrorLogError() throws Exception {
    ReflectionTestUtils.setField(cut, "log", testLogger);
    ClientHttpResponse response = mock(ClientHttpResponse.class);
    when(response.getStatusCode()).thenReturn(HttpStatus.METHOD_NOT_ALLOWED);
    when(response.getBody())
        .thenReturn(new ByteArrayInputStream("MOCK_ERROR_MSG".getBytes(StandardCharsets.UTF_8)));
    assertThrows(ConverterServiceException.class, () -> cut.handleError(response));
    assertThat(testLogger.getLoggingEvents().size()).isOne();
  }

  @Test
  @DisplayName("test handle 5xx related exception")
  void testHandle5XxRelatedException() throws Exception {
    ReflectionTestUtils.setField(cut, "log", testLogger);
    ClientHttpResponse response = mock(ClientHttpResponse.class);
    when(response.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
    when(response.getBody())
        .thenReturn(new ByteArrayInputStream("MOCK_ERROR_MSG".getBytes(StandardCharsets.UTF_8)));
    assertThrows(ConverterServiceException.class, () -> cut.handleError(response));
    assertThat(testLogger.getLoggingEvents().size()).isOne();
  }

  @Test
  @DisplayName("test handle not found exception")
  void testHandleNotFoundException() throws Exception {
    ReflectionTestUtils.setField(cut, "log", testLogger);
    ClientHttpResponse response = mock(ClientHttpResponse.class);
    when(response.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
    when(response.getBody())
        .thenReturn(new ByteArrayInputStream("MOCK_ERROR_MSG".getBytes(StandardCharsets.UTF_8)));
    assertThrows(NotFoundException.class, () -> cut.handleError(response));
    assertThat(testLogger.getLoggingEvents().size()).isOne();
  }
}
