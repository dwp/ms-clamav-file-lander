package uk.gov.dwp.health.clamav.http.pdfconvertor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PdfConvertRequestTest {

  private PdfConvertRequest underTest;

  @Test
  void testCreateJsonStringFromObject() {
    underTest = new PdfConvertRequest();
    underTest.setId("testId");
    underTest.setFileName("fileName");
    underTest.setS3Ref("tests3ref");
    underTest.setSrcBucket("pip-bucket");
    underTest.setDestBucket("pip-bucket");
    underTest.setDestEncryptEnabled(true);
    underTest.setScrEncryptEnabled(true);
    assertThat(underTest.toJson())
        .isEqualTo(
            "{\"id\":\"testId\",\"fileName\":\"fileName\",\"s3Ref\":\"tests3ref\",\"srcBucket\":\"pip-bucket\",\"destBucket\":\"pip-bucket\",\"scrEncryptEnabled\":true,\"destEncryptEnabled\":true}");
  }

  @Test
  void testWhenExceptionThrowErrorLoggedEmptyJsonReturned() throws Exception {
    underTest = new PdfConvertRequest();
    ObjectMapper mapper = mock(ObjectMapper.class);
    ReflectionTestUtils.setField(underTest, "mapper", mapper);
    when(mapper.writeValueAsString(any(PdfConvertRequest.class)))
        .thenThrow(new JsonProcessingException("") {});
    String actual = underTest.toJson();
    assertThat(actual).isEqualTo("{}");
  }
}
