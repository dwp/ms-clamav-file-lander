package uk.gov.dwp.health.clamav.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.dwp.health.clamav.exception.ClamAvServiceException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClamAvClientImplTest {

  private ClamAvClientImpl cut;
  private Socket socket;

  private static Stream<Arguments> createFoundOrOKFixture() {
    return Stream.of(Arguments.of("OK", false), Arguments.of("FOUND", true));
  }

  private static Stream<Arguments> createErrorFixture() {
    return Stream.of(Arguments.of("ERROR"), Arguments.of("UNKNOWN"));
  }

  @BeforeEach
  void setup() {
    socket = mock(Socket.class);
    cut = new ClamAvClientImpl(socket);
    ReflectionTestUtils.setField(cut, "socket", socket);
  }

  @ParameterizedTest
  @MethodSource("createFoundOrOKFixture")
  void testClamAvRespondFoundOrOK(String response, boolean expected) throws IOException {
    InputStream inputStream = mock(InputStream.class);
    OutputStream outputStream = mock(OutputStream.class);
    when(inputStream.readAllBytes()).thenReturn(response.getBytes());
    when(inputStream.read(any())).thenReturn(-1);
    when(socket.getInputStream()).thenReturn(inputStream);
    when(socket.getOutputStream()).thenReturn(outputStream);
    boolean actual = cut.scanForVirus(inputStream);
    assertThat(actual).isEqualTo(expected);
  }

  @ParameterizedTest
  @MethodSource("createErrorFixture")
  void testClamAvRespondWithErrorExceptionThrown(String response) throws IOException {
    InputStream inputStream = mock(InputStream.class);
    OutputStream outputStream = mock(OutputStream.class);
    when(inputStream.readAllBytes()).thenReturn(response.getBytes());
    when(inputStream.read(any())).thenReturn(-1);
    when(socket.getInputStream()).thenReturn(inputStream);
    when(socket.getOutputStream()).thenReturn(outputStream);
    Assertions.assertThrows(ClamAvServiceException.class, () -> cut.scanForVirus(inputStream));
  }

  @Test
  void testIOErrorClamAvServiceExceptionThrown() throws IOException {
    InputStream inputStream = mock(InputStream.class);
    when(socket.getOutputStream()).thenThrow(IOException.class);
    Assertions.assertThrows(ClamAvServiceException.class, () -> cut.scanForVirus(inputStream));
  }
}
