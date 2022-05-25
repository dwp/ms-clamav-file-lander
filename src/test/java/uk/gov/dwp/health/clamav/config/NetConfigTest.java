package uk.gov.dwp.health.clamav.config;

import org.junit.jupiter.api.Test;
import uk.gov.dwp.health.clamav.config.properties.NetConfigProperties;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NetConfigTest {

  @SuppressWarnings("java:S2970")
  @Test
  void testCreateSocketBeanOnRequestWithGivenHostPort() {
    NetConfig cut = new NetConfig();
    NetConfigProperties props = mock(NetConfigProperties.class);
    when(props.getHost()).thenReturn("localhost");
    when(props.getPort()).thenReturn(3310);
    try {
      cut.socket(props);
    } catch (IOException ex) {
      verify(props, times(1)).getHost();
      verify(props, times(1)).getPort();
    }
  }
}
