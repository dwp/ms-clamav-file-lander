package uk.gov.dwp.health.clamav.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.dwp.health.crypto.CryptoDataManager;
import uk.gov.dwp.health.crypto.CryptoMessage;
import uk.gov.dwp.health.crypto.exception.CryptoException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KmsServiceImplTest {

  @Captor ArgumentCaptor<String> strArgCaptor;
  @InjectMocks private KmsServiceImpl cut;
  @Mock private CryptoDataManager manager;

  @Test
  void testKmsEncryptionContent() throws CryptoException {
    byte[] content = "TEST".getBytes();
    CryptoMessage cryptoMessage = mock(CryptoMessage.class);
    when(manager.encrypt(anyString())).thenReturn(cryptoMessage);
    cut.encrypt(content);
    verify(manager).encrypt(strArgCaptor.capture());
    assertThat(strArgCaptor.getValue()).isEqualTo("VEVTVA==");
  }
}
