package uk.gov.dwp.health.clamav.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import uk.gov.dwp.health.clamav.config.properties.S3ConfigProperties;
import uk.gov.dwp.health.clamav.exception.S3FileUploadException;
import uk.gov.dwp.health.crypto.CryptoMessage;
import uk.gov.dwp.health.crypto.exception.CryptoException;

import java.io.IOException;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3FileWriterImplTest {
  @Captor ArgumentCaptor<RequestBody> requestBodyArgumentCaptor;
  @Captor private ArgumentCaptor<Consumer> consumerArgumentCaptor;
  @InjectMocks private S3FileWriterImpl cut;
  @Mock private S3Client s3Client;
  @Mock private S3ConfigProperties s3props;
  @Mock private ObjectMapper objectMapper;
  @Mock private KmsServiceImpl kmsService;

  @BeforeEach
  void setup() {
    cut = new S3FileWriterImpl(s3Client, s3props, kmsService, objectMapper);
  }

  @Test
  void testAwsS3UploadIllegalStateExceptionThrown() throws Exception {
    MultipartFile file = mock(MultipartFile.class);
    assertThrows(IllegalArgumentException.class, () -> cut.write("", file));
    verifyNoInteractions(s3Client);
  }

  @Test
  void testAwsS3UploadThrowFileUploadException() throws CryptoException {
    MultipartFile file = mock(MultipartFile.class);
    when(file.getOriginalFilename()).thenReturn("test.pdf");
    when(s3props.isEncryptEnable()).thenReturn(true);
    when(kmsService.encrypt(any())).thenThrow(CryptoException.class);
    assertThrows(S3FileUploadException.class, () -> cut.write("123", file));
  }

  @Test
  void testUploadToS3() throws IOException, CryptoException {
    CryptoMessage cryptoMessage = mock(CryptoMessage.class);
    MultipartFile file = mock(MultipartFile.class);
    when(file.getOriginalFilename()).thenReturn("test.pdf");
    when(file.getBytes()).thenReturn(new byte[1]);
    when(file.getContentType()).thenReturn("application/pdf");
    when(objectMapper.writeValueAsBytes(any(CryptoMessage.class))).thenReturn(new byte[3]);
    when(s3props.isEncryptEnable()).thenReturn(true);
    when(kmsService.encrypt(any())).thenReturn(cryptoMessage);
    doAnswer(
            answer -> {
              Consumer<PutObjectRequest.Builder> consumer = answer.getArgument(0);
              consumer.accept(PutObjectRequest.builder().bucket("mock_bucket").key("file"));
              return null;
            })
        .when(s3Client)
        .putObject(any(Consumer.class), any(RequestBody.class));
    cut.write("test", file);
    verify(s3Client)
        .putObject(consumerArgumentCaptor.capture(), requestBodyArgumentCaptor.capture());
    assertThat(requestBodyArgumentCaptor.getValue().contentType())
        .isEqualTo(MediaType.APPLICATION_OCTET_STREAM_VALUE);
    assertThat(requestBodyArgumentCaptor.getValue().contentLength()).isEqualTo(3L);
  }

  @ParameterizedTest
  @NullAndEmptySource
  void testUploadThrowsTaskExceptionDueToEmptyOrNullKey(String key) {
    MultipartFile file = mock(MultipartFile.class);
    assertThrows(IllegalArgumentException.class, () -> cut.write(key, file));
  }

  @ParameterizedTest
  @NullSource
  void testUploadThrowsTaskExceptionDueToIllegalCryptoOrObjectKeyNull(MultipartFile file) {
    assertThrows(IllegalArgumentException.class, () -> cut.write("test", file));
  }

  @Test
  @DisplayName("test to throw file verification exception")
  void testToThrowFileVerificationException() throws IOException, CryptoException {
    var id = "file-id";
    CryptoMessage cryptoMessage = mock(CryptoMessage.class);
    MultipartFile file = mock(MultipartFile.class);
    when(file.getOriginalFilename()).thenReturn("test.pdf");
    when(file.getBytes()).thenReturn(new byte[1]);
    when(file.getContentType()).thenReturn("application/pdf");
    when(objectMapper.writeValueAsBytes(any(CryptoMessage.class))).thenReturn(new byte[3]);
    when(s3props.isEncryptEnable()).thenReturn(true);
    when(kmsService.encrypt(any())).thenReturn(cryptoMessage);
    var response = mock(PutObjectResponse.class);
    when(response.eTag()).thenReturn(null);
    doAnswer(
            answer -> {
              Consumer<PutObjectRequest.Builder> consumer = answer.getArgument(0);
              consumer.accept(PutObjectRequest.builder().bucket("mock_bucket").key("file"));
              return response;
            })
        .when(s3Client)
        .putObject(any(Consumer.class), any(RequestBody.class));
    assertThrows(S3FileUploadException.class, () -> cut.write(id, file));
  }
}
