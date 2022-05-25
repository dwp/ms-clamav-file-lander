package uk.gov.dwp.health.clamav.config.properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.services.s3.S3Client;
import uk.gov.dwp.health.clamav.Application;
import uk.gov.dwp.health.clamav.exception.UnknownFileTypeException;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles(profiles = {"config"})
@SpringBootTest(
    classes = {Application.class, CVServiceConfigProperties.class},
    properties = {
      "aws.s3.bucket=mock-bucket",
      "aws.s3.awsRegion=eu-west-2",
      "aws.encryption.dataKey=test-key",
    })
class CVServiceConfigPropertiesTest {

  @MockBean S3Client s3ClientMock;
  @Autowired private CVServiceConfigProperties cut;

  @Test
  @DisplayName("Test load service configurations from an external configuration file")
  void testLoadServiceConfigurationsFromAnExternalConfigurationFile() {
    final List<CVService> actual = cut.getServices();
    assertAll(
        "assert configurations",
        () -> assertEquals("test-ms-word-pdf", actual.get(0).getName()),
        () -> assertEquals("test-image-pdf", actual.get(1).getName()),
        () -> assertEquals("/v1/convert/s3", actual.get(0).getEndpoint()),
        () -> assertEquals("/v1/convert/s3", actual.get(1).getEndpoint()),
        () -> assertEquals("http://word2pdf", actual.get(0).getUri()),
        () -> assertEquals("http://img2pdf", actual.get(1).getUri()),
        () ->
            assertEquals(
                Set.of(
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
                actual.get(0).getTypes()),
        () -> assertEquals(Set.of("image/jpeg", "image/png"), actual.get(1).getTypes()));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
      })
  @DisplayName("Test found word 2 pdf service")
  void testFoundWord2PdfService(final String mime) {
    CVService actual = cut.findServiceByMimeType(mime);
    assertAll(
        "assert service",
        () -> assertNotNull(actual),
        () -> assertEquals("http://word2pdf", actual.getUri()),
        () -> assertEquals("/v1/convert/s3", actual.getEndpoint()),
        () -> assertEquals("test-ms-word-pdf", actual.getName()));
  }

  @ParameterizedTest
  @ValueSource(strings = {"image/jpeg", "image/png"})
  @DisplayName("Test found image 2 pdf service")
  void testFoundImage2PdfService(final String mime) {
    CVService actual = cut.findServiceByMimeType(mime);
    assertAll(
        "assert service",
        () -> assertNotNull(actual),
        () -> assertEquals("http://img2pdf", actual.getUri()),
        () -> assertEquals("/v1/convert/s3", actual.getEndpoint()),
        () -> assertEquals("test-image-pdf", actual.getName()));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @DisplayName("test empty null mime thrown illegal exception")
  void testEmptyNullMimeThrownIllegalException(final String mime) {
    assertThrows(IllegalArgumentException.class, () -> cut.findServiceByMimeType(mime));
  }

  @Test
  @DisplayName("test an unknown mime throws UnknownFileTypeException ")
  void testAnUnknownMimeThrowsUnknownFileTypeException() {
    assertThrows(UnknownFileTypeException.class, () -> cut.findServiceByMimeType("unknown/mime"));
  }
}
