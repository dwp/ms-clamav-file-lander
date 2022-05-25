package uk.gov.dwp.health.clamav.config;

import com.amazonaws.services.kms.model.NotFoundException;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;
import uk.gov.dwp.health.clamav.exception.ConverterServiceException;

import java.io.IOException;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.Series.CLIENT_ERROR;
import static org.springframework.http.HttpStatus.Series.SERVER_ERROR;

@NoArgsConstructor
@Component
public class HttpRespStatusHandler implements ResponseErrorHandler {

  private static Logger log = LoggerFactory.getLogger(HttpRespStatusHandler.class);

  @Override
  public void handleError(ClientHttpResponse response) throws IOException {
    if (response.getStatusCode().series() == SERVER_ERROR) {
      final String msg =
          String.format("Server error - Response code [%s]", response.getStatusCode().value());
      log.error(msg);
      throw new ConverterServiceException(msg);
    } else if (response.getStatusCode().series() == CLIENT_ERROR) {
      if (response.getStatusCode() == NOT_FOUND) {
        final String msg =
            String.format("Client error - Response code [%s]", response.getStatusCode());
        log.error(msg);
        throw new NotFoundException(msg);
      }
      final String msg =
          String.format("Client error - Response code [%s]", response.getStatusCode());
      log.error(msg);
      throw new ConverterServiceException(msg);
    }
  }

  @Override
  public boolean hasError(ClientHttpResponse response) throws IOException {
    return response.getStatusCode().series() == CLIENT_ERROR
        || response.getStatusCode().series() == SERVER_ERROR;
  }
}
