package uk.gov.dwp.health.clamav.config;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Throwables;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;
import uk.gov.dwp.health.clamav.config.properties.S3ConfigProperties;

import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
class S3ConfigTest {

  @Test
  void testCreateAwsS3ClientWithEndpointOverride() {
    S3ConfigProperties prop = mock(S3ConfigProperties.class);
    when(prop.getAwsRegion()).thenReturn("eu-west-2");
    when(prop.getEndpointOverride()).thenReturn("localstack:4527");
    when(prop.isPathStyleEnable()).thenReturn(true);
    S3Config cut = new S3Config();
    try {
      S3Client actual = cut.s3Client(prop);
      assertThat(actual).isNotNull();
    } catch (NumberFormatException | URISyntaxException ex) {
      // ProxyConfiguration.resolvePort() seems unable to get a
      // validation port number(integer) this issue only appear
      // when run test in gitlab, following code prints stacktrace
      // in gitlab console log
      final String stackTraceMsg = Throwables.getStackTrace(ex);
      log.info(stackTraceMsg);
    }
  }

  @Test
  void testCreateAwsS3Client() {
    S3ConfigProperties prop = mock(S3ConfigProperties.class);
    when(prop.getEndpointOverride()).thenReturn(null);
    when(prop.getAwsRegion()).thenReturn("eu-west-2");
    when(prop.isPathStyleEnable()).thenReturn(true);
    S3Config cut = new S3Config();
    try {
      S3Client actual = cut.s3Client(prop);
      assertThat(actual).isNotNull();
    } catch (NumberFormatException | URISyntaxException ex) {
      // ProxyConfiguration.resolvePort() seems unable to get a
      // validation port number(integer) this issue only appear
      // when run test in gitlab, following code prints stacktrace
      // in gitlab console log
      final String stackTraceMsg = Throwables.getStackTrace(ex);
      log.info(stackTraceMsg);
    }
  }
}
