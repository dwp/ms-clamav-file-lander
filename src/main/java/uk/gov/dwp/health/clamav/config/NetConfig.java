package uk.gov.dwp.health.clamav.config;

import lombok.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import uk.gov.dwp.health.clamav.config.properties.NetConfigProperties;

import java.io.IOException;
import java.net.Socket;

@Generated
@Configuration
public class NetConfig {

  @Lazy
  @Bean
  @Scope(scopeName = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
  public Socket socket(@Autowired NetConfigProperties props) throws IOException {
    return new Socket(props.getHost(), props.getPort());
  }
}
