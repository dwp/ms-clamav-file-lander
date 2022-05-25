package uk.gov.dwp.health.clamav.utils;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.dwp.health.clamav.constant.MSWordMediaType;
import uk.gov.dwp.health.clamav.exception.FilePasswordProtectedException;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FileUtilsTest {

  private static FileUtils cut;

  @BeforeAll
  static void setupSpec() {
    cut = new FileUtils();
  }

  private static Stream<Arguments> testFixture() {
    return Stream.of(
        Arguments.of(0, 0),
        Arguments.of(1, 1),
        Arguments.of(1023, 1),
        Arguments.of(1024, 1),
        Arguments.of(1025, 2));
  }

  @ParameterizedTest(name = "{index} file size {0} is expected to be {1}")
  @MethodSource(value = "testFixture")
  @DisplayName("Test calculate file size in kb given an integer")
  void testCalculateFileSizeInKbGivenAnInteger(int input, int expected) {
    assertThat(cut.fileSizeInKb(input)).isEqualTo(expected);
  }

  @Test
  @DisplayName("Test illegal input and IllegalArgExceptionThrown")
  void testIllegalInputAndIllegalArgExceptionThrown() {
    assertThrows(IllegalArgumentException.class, () -> cut.fileSizeInKb(-1));
  }

  @Nested
  @DisplayName("Tests for files are password protected")
  class TestPasswordProtectedFile {
    @Test
    @DisplayName("test load PDF file password protected throws exception")
    void testLoadPdfFilePasswordProtectedThrowsException() throws IOException {
      InputStream is = getClass().getClassLoader().getResourceAsStream("Protected-PDF.pdf");
      MultipartFile multipartFile = mock(MultipartFile.class);
      when(multipartFile.getInputStream()).thenReturn(is);
      when(multipartFile.getContentType()).thenReturn(MediaType.APPLICATION_PDF_VALUE);
      assertThrows(
          FilePasswordProtectedException.class,
          () -> cut.validatePasswordProtection(multipartFile));
    }

    @Test
    @DisplayName("test load docx file password protected throws exception")
    void testLoadDocxFilePasswordProtectedThrowsException() throws IOException {
      InputStream is = getClass().getClassLoader().getResourceAsStream("Protected-wordx.docx");
      MultipartFile multipartFile = mock(MultipartFile.class);
      when(multipartFile.getInputStream()).thenReturn(is);
      when(multipartFile.getContentType()).thenReturn(MSWordMediaType.APPLICATION_MS_DOCX);
      assertThrows(
          FilePasswordProtectedException.class,
          () -> cut.validatePasswordProtection(multipartFile));
    }

    @Test
    @DisplayName("test load doc file password protected throws exception")
    void testLoadDocFilePasswordProtectedThrowsException() throws IOException {
      InputStream is = getClass().getClassLoader().getResourceAsStream("Protected-word.doc");
      MultipartFile multipartFile = mock(MultipartFile.class);
      when(multipartFile.getInputStream()).thenReturn(is);
      when(multipartFile.getContentType()).thenReturn(MSWordMediaType.APPLICATION_MS_DOC);
      assertThrows(
          FilePasswordProtectedException.class,
          () -> cut.validatePasswordProtection(multipartFile));
    }
  }

  @Nested
  @DisplayName("Tests for files are not password protected")
  class TestNonePasswordProtectedFile {
    @Test
    @DisplayName("test load PDF file not password protected")
    void testLoadPdfFileNotPasswordProtected() throws IOException {
      InputStream is = getClass().getClassLoader().getResourceAsStream("Unprotected-PDF.pdf");
      MultipartFile multipartFile = mock(MultipartFile.class);
      when(multipartFile.getInputStream()).thenReturn(is);
      when(multipartFile.getContentType()).thenReturn(MediaType.APPLICATION_PDF_VALUE);
      cut.validatePasswordProtection(multipartFile);
    }

    @Test
    @DisplayName("test load DOCX file not password protected")
    void testLoadDocxFileNotPasswordProtected() throws IOException {
      InputStream is = getClass().getClassLoader().getResourceAsStream("Unprotected-wordx.docx");
      MultipartFile multipartFile = mock(MultipartFile.class);
      when(multipartFile.getInputStream()).thenReturn(is);
      when(multipartFile.getContentType()).thenReturn(MSWordMediaType.APPLICATION_MS_DOCX);
      cut.validatePasswordProtection(multipartFile);
    }

    @Test
    @DisplayName("test load DOC file not password protected")
    void testLoadDocFileNotPasswordProtected() throws IOException {
      InputStream is = getClass().getClassLoader().getResourceAsStream("Unprotected-word.doc");
      MultipartFile multipartFile = mock(MultipartFile.class);
      when(multipartFile.getInputStream()).thenReturn(is);
      when(multipartFile.getContentType()).thenReturn(MSWordMediaType.APPLICATION_MS_DOC);
      cut.validatePasswordProtection(multipartFile);
    }
  }
}
