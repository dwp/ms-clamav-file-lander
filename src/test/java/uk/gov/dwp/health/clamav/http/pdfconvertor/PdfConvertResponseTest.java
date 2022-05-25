package uk.gov.dwp.health.clamav.http.pdfconvertor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PdfConvertResponseTest {

  private PdfConvertResponse underTest;

  @Test
  void testCreateJsonStringFromObject() {
    underTest = new PdfConvertResponse();
    underTest.setS3Ref("tests3ref");
    underTest.setSuccess(true);
    underTest.setFileSizeKb(300);
    underTest.setBucketName("pip-bucket");
    assertThat(underTest.toJson())
        .isEqualTo(
            "{\"s3Ref\":\"tests3ref\",\"bucketName\":\"pip-bucket\",\"success\":true,\"message\":null,\"fileSizeKb\":300}");
  }

  @Test
  void testWhenExceptionThrowErrorLoggedEmptyJsonReturned() throws Exception {
    underTest = new PdfConvertResponse();
    ObjectMapper mapper = mock(ObjectMapper.class);
    ReflectionTestUtils.setField(underTest, "mapper", mapper);
    when(mapper.writeValueAsString(any(PdfConvertResponse.class)))
        .thenThrow(new JsonProcessingException("") {});
    String actual = underTest.toJson();
    assertThat(actual).isEqualTo("{}");
  }
}
