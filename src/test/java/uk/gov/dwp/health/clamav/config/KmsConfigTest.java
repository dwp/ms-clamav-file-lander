package uk.gov.dwp.health.clamav.config;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.kms.model.DataKeySpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.dwp.health.clamav.config.properties.CryptoConfigProperties;
import uk.gov.dwp.health.clamav.exception.CryptoConfigException;
import uk.gov.dwp.health.crypto.CryptoConfig;
import uk.gov.dwp.health.crypto.CryptoDataManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KmsConfigTest {

  private KmsConfig underTest;

  @BeforeEach
  void setup() {
    underTest = new KmsConfig();
  }

  @Test
  void testConfigurationInvalidCryptoConfigExceptionThrown() {
    CryptoConfig config = mock(CryptoConfig.class);
    when(config.isContentValid()).thenReturn(false);
    Assertions.assertThrows(CryptoConfigException.class, () -> underTest.cryptoDataManager(config));
  }

  @Test
  void testCreateCryptoConfigBeanWithKmsOverride() {
    CryptoConfigProperties props = mock(CryptoConfigProperties.class);
    given(props.getKmsOverride()).willReturn("http://localhost", "http://localhost");
    given(props.getDataKey()).willReturn("kms_data_key");
    CryptoConfig actual = underTest.cryptoConfig(props);
    assertThat(actual).isNotNull();
    assertThat(actual.getDataKeyId()).isEqualTo("kms_data_key");
    assertThat(actual.getKmsEndpointOverride()).isEqualTo("http://localhost");
  }

  @Test
  void testCreateCryptoConfigBeanWithoutKmsDefault() {
    CryptoConfigProperties props = mock(CryptoConfigProperties.class);
    given(props.getKmsOverride()).willReturn(null);
    given(props.getDataKey()).willReturn("kms_data_key");
    CryptoConfig actual = underTest.cryptoConfig(props);
    assertThat(actual).isNotNull();
    assertThat(actual.getDataKeyId()).isEqualTo("kms_data_key");
    assertThat(actual.getKmsEndpointOverride()).isNull();
  }

  @Test
  void testCreateCryptoManagerBean() {
    CryptoConfig config = mock(CryptoConfig.class);
    given(config.isContentValid()).willReturn(true);
    given(config.getEncryptionType()).willReturn(DataKeySpec.AES_256.name());
    given(config.getRegion()).willReturn(Regions.EU_WEST_2);
    CryptoDataManager actual = underTest.cryptoDataManager(config);
    assertThat(actual).isNotNull();
  }

  @Test
  void testCreateCryptoConfigWithCustomPropValues() {
    CryptoConfigProperties props = mock(CryptoConfigProperties.class);
    given(props.getKmsOverride()).willReturn("http://localhost");
    CryptoConfig actual = underTest.cryptoConfig(props);
    assertThat(actual).isNotNull();
    assertThat(actual.getRegion()).isEqualTo(Regions.EU_WEST_2);
    assertThat(actual.getKmsEndpointOverride()).isEqualTo("http://localhost");
  }

  @Test
  void testCreateCryptoConfigWithoutPropValues() {
    CryptoConfigProperties props = mock(CryptoConfigProperties.class);
    given(props.getKmsOverride()).willReturn("");
    CryptoConfig actual = underTest.cryptoConfig(props);
    assertThat(actual).isNotNull();
    assertThat(actual.getRegion()).isEqualTo(Regions.EU_WEST_2);
    assertThat(actual.getKmsEndpointOverride()).isNull();
  }
}
