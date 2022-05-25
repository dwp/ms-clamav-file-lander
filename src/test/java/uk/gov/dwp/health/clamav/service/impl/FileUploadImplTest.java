package uk.gov.dwp.health.clamav.service.impl;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.dwp.health.clamav.config.properties.S3ConfigProperties;
import uk.gov.dwp.health.clamav.constant.MSWordMediaType;
import uk.gov.dwp.health.clamav.exception.ConverterServiceException;
import uk.gov.dwp.health.clamav.exception.FailReadUploadFileException;
import uk.gov.dwp.health.clamav.exception.FilePasswordProtectedException;
import uk.gov.dwp.health.clamav.exception.VirusDetectionException;
import uk.gov.dwp.health.clamav.http.pdfconvertor.PdfConvertRequest;
import uk.gov.dwp.health.clamav.http.pdfconvertor.PdfConvertResponse;
import uk.gov.dwp.health.clamav.openapi.model.FileUploadResponse;
import uk.gov.dwp.health.clamav.openapi.model.ServiceRequestObject;
import uk.gov.dwp.health.clamav.service.ClamAvClientService;
import uk.gov.dwp.health.clamav.utils.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileUploadImplTest {

  @InjectMocks private FileUploadImpl cut;
  @Mock private S3FileWriterImpl s3FileWriter;
  @Mock private ClamAvClientService clamAvClientService;
  @Mock private S3ConfigProperties s3ConfigProperties;
  @Spy private FileUtils fileUtils = new FileUtils();
  @Mock private PdfConvertorClientServiceImpl pdfClientService;
  private ServiceRequestObject requestObject = mock(ServiceRequestObject.class);
  private MultipartFile multipartFile = mock(MultipartFile.class);

  @Captor ArgumentCaptor<PdfConvertRequest> convertReqArgCaptor;
  @Captor ArgumentCaptor<String> stringArgCaptor;

  private static InputStream UNPROTECTED_DOC;
  private static InputStream UNPROTECTED_DOCX;
  private static InputStream PROTECTED_DOC;
  private static InputStream UNPROTECTED_PDF;
  private static InputStream PROTECTED_PDF;

  @BeforeAll
  static void setupSpec() {
    ClassLoader classLoader = FileUploadImplTest.class.getClassLoader();
    UNPROTECTED_DOC = classLoader.getResourceAsStream("Unprotected-word.doc");
    UNPROTECTED_DOCX = classLoader.getResourceAsStream("Unprotected-wordx.docx");
    UNPROTECTED_PDF = classLoader.getResourceAsStream("Unprotected-PDF.pdf");
    PROTECTED_PDF = classLoader.getResourceAsStream("Protected-PDF.pdf");
    PROTECTED_DOC = classLoader.getResourceAsStream("Protected-word.doc");
  }

  @Nested
  @DisplayName("Tests for files are not password protected")
  class TestSuccessfulFileUpload {
    @Test
    void testSuccessUploadImageFileWithConversion() throws IOException {
      PdfConvertResponse pdfResponse = new PdfConvertResponse();
      pdfResponse.setSuccess(true);
      pdfResponse.setFileSizeKb(900);
      pdfResponse.setS3Ref("CV_file_unique_s3_ref");
      pdfResponse.setBucketName("testbucket");
      when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[1]));
      when(multipartFile.getContentType()).thenReturn("image/jpeg");
      when(clamAvClientService.scanForVirus(any())).thenReturn(false);
      when(s3FileWriter.write(any(), any())).thenReturn("file_unique_s3_ref");
      when(s3ConfigProperties.getBucket()).thenReturn("pip-bucket");
      when(requestObject.isPersist()).thenReturn(true);
      when(pdfClientService.postConvertRequest(any(PdfConvertRequest.class), anyString()))
          .thenReturn(pdfResponse);
      FileUploadResponse actual = cut.handleFileLanding(requestObject, multipartFile);
      assertThat(actual.getS3Ref()).isEqualTo("CV_file_unique_s3_ref");
      assertThat(actual.getBucket()).isEqualTo("testbucket");
      assertThat(actual.getFileSizeKb()).isEqualTo(900);
    }

    @Test
    void testSuccessUploadPdfFileWithoutConversion() throws IOException {
      when(multipartFile.getInputStream()).thenReturn(UNPROTECTED_PDF);
      when(multipartFile.getContentType()).thenReturn("application/pdf");
      when(clamAvClientService.scanForVirus(any())).thenReturn(false);
      when(s3FileWriter.write(any(), any())).thenReturn("file_unique_s3_ref");
      when(s3ConfigProperties.getBucket()).thenReturn("pip-bucket");
      when(requestObject.isPersist()).thenReturn(true);
      when(fileUtils.fileSizeInKb(anyInt())).thenReturn(1);
      FileUploadResponse actual = cut.handleFileLanding(requestObject, multipartFile);
      assertThat(actual.getS3Ref()).isEqualTo("file_unique_s3_ref");
      assertThat(actual.getBucket()).isEqualTo("pip-bucket");
      assertThat(actual.getFileSizeKb()).isOne();
      verifyNoInteractions(pdfClientService);
    }

    @Test
    void testSuccessFileUploadWithoutS3PersistRequired() throws IOException {
      when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[1]));
      when(multipartFile.getContentType()).thenReturn(MediaType.APPLICATION_PDF_VALUE);
      when(clamAvClientService.scanForVirus(any())).thenReturn(false);
      when(requestObject.isPersist()).thenReturn(false);
      FileUploadResponse actual = cut.handleFileLanding(requestObject, multipartFile);
      assertThat(actual.getS3Ref()).isNull();
      assertThat(actual.getBucket()).isNull();
      verifyNoInteractions(s3FileWriter);
      verifyNoInteractions(s3ConfigProperties);
    }

    @ParameterizedTest
    @ArgumentsSource(UnprotectedMsDocs.class)
    @DisplayName("test upload unprotected doc and docx")
    void testUploadUnprotectedDocOrDocxFile(String type, InputStream input) throws IOException {
      PdfConvertResponse pdfResponse = new PdfConvertResponse();
      when(multipartFile.getInputStream()).thenReturn(input);
      when(multipartFile.getContentType()).thenReturn(type);
      when(clamAvClientService.scanForVirus(any())).thenReturn(false);
      when(s3ConfigProperties.getBucket()).thenReturn("pip-bucket");
      when(requestObject.getFileName()).thenReturn("mock-file-name");
      when(requestObject.isPersist()).thenReturn(true);
      when(pdfClientService.postConvertRequest(any(PdfConvertRequest.class), anyString()))
          .thenReturn(pdfResponse);
      cut.handleFileLanding(requestObject, multipartFile);
      verify(pdfClientService)
          .postConvertRequest(convertReqArgCaptor.capture(), stringArgCaptor.capture());
      PdfConvertRequest capturedReq = convertReqArgCaptor.getValue();
      assertAll(
          "Assert all values",
          () -> {
            assertThat(capturedReq.getFileName()).isEqualTo("mock-file-name");
            assertThat(capturedReq.getDestBucket()).isEqualTo("pip-bucket");
            assertThat(capturedReq.getSrcBucket()).isEqualTo("pip-bucket");
            assertThat(stringArgCaptor.getValue()).isEqualTo(type);
          });
    }
  }

  static class UnprotectedMsDocs implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of(MSWordMediaType.APPLICATION_MS_DOC, UNPROTECTED_DOC),
          Arguments.of(MSWordMediaType.APPLICATION_MS_DOCX, UNPROTECTED_DOCX));
    }
  }

  @Nested
  @DisplayName("Tests for files are rejected failed during upload")
  class TestFailureFileUpload {

    @Test
    @DisplayName("test upload protected doc and docx throws exception")
    void testUploadProtectedDocAndDocx() throws IOException {
      when(multipartFile.getInputStream()).thenReturn(PROTECTED_DOC);
      when(multipartFile.getContentType()).thenReturn(MSWordMediaType.APPLICATION_MS_DOC);
      when(clamAvClientService.scanForVirus(any())).thenReturn(false);
      when(requestObject.isPersist()).thenReturn(true);
      assertThrows(
          FilePasswordProtectedException.class,
          () -> cut.handleFileLanding(requestObject, multipartFile));
      verifyNoInteractions(pdfClientService);
    }

    @Test
    @DisplayName("test upload protected pdf file throws exception")
    void testPdfFileIsProtectedAndThrowsException() throws IOException {
      when(multipartFile.getInputStream()).thenReturn(PROTECTED_PDF);
      when(multipartFile.getContentType()).thenReturn("application/pdf");
      when(clamAvClientService.scanForVirus(any())).thenReturn(false);
      when(requestObject.isPersist()).thenReturn(true);
      assertThrows(
          FilePasswordProtectedException.class,
          () -> cut.handleFileLanding(requestObject, multipartFile));
      verifyNoInteractions(pdfClientService);
    }

    @Test
    void testFileUploadConvertorServicesFail() throws IOException {
      when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[1]));
      when(multipartFile.getContentType()).thenReturn("image/jpeg");
      when(clamAvClientService.scanForVirus(any())).thenReturn(false);
      when(s3FileWriter.write(any(), any())).thenReturn("file_unique_s3_ref");
      when(s3ConfigProperties.getBucket()).thenReturn("pip-bucket");
      when(requestObject.isPersist()).thenReturn(true);
      when(pdfClientService.postConvertRequest(any(PdfConvertRequest.class), anyString()))
          .thenThrow(new ConverterServiceException("pdf conversion failed"));
      var ex =
          assertThrows(
              ConverterServiceException.class,
              () -> cut.handleFileLanding(requestObject, multipartFile));
      assertEquals("pdf conversion failed", ex.getMessage());
    }

    @Test
    void testAvScanVirusDetectedVirusDetectedExceptionThrown() throws IOException {
      when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[1]));
      when(clamAvClientService.scanForVirus(any())).thenReturn(true);
      assertThrows(
          VirusDetectionException.class, () -> cut.handleFileLanding(requestObject, multipartFile));
    }

    @Test
    void testFailReadFromMultipartFileFailReadFileExceptionThrown() throws IOException {
      IOException exception = mock(IOException.class);
      when(multipartFile.getInputStream()).thenThrow(exception);
      assertThrows(
          FailReadUploadFileException.class,
          () -> cut.handleFileLanding(requestObject, multipartFile));
    }
  }
}
