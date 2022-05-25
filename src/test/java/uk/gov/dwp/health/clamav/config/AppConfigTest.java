package uk.gov.dwp.health.clamav.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.dwp.health.clamav.utils.FileUtils;

import javax.validation.Validator;

import static org.assertj.core.api.Assertions.assertThat;

class AppConfigTest {

  private static AppConfig cut;

  @BeforeAll
  static void setupSpec() {
    cut = new AppConfig();
  }

  @Test
  void testCreateObjectMapper() {
    ObjectMapper actual = cut.objectMapper();
    assertThat(actual).isNotNull().isInstanceOf(ObjectMapper.class);
  }

  @Test
  @DisplayName("Test get validator")
  void testGetValidator() {
    Validator actual = cut.validator();
    assertThat(actual).isNotNull().isInstanceOf(Validator.class);
  }

  @Test
  @DisplayName("Test file size utils bean")
  void testFileSizeUtilsBean() {
    assertThat(cut.fileUtils()).isNotNull().isInstanceOf(FileUtils.class);
  }
}
