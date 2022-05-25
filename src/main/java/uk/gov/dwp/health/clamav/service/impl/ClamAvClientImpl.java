package uk.gov.dwp.health.clamav.service.impl;

import lombok.Generated;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.dwp.health.clamav.exception.ClamAvServiceException;
import uk.gov.dwp.health.clamav.service.ClamAvClientService;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Generated
@Slf4j
@Service
public class ClamAvClientImpl implements ClamAvClientService {

  private static final int TIME_OUT = 7000;
  private static final int BUFFER_SIZE = 2018;

  private final Socket socket;

  public ClamAvClientImpl(Socket socket) {
    this.socket = socket;
  }

  @Override
  public boolean scanForVirus(final InputStream stream) {
    try (OutputStream outputStream = new BufferedOutputStream(socket.getOutputStream())) {
      socket.setSoTimeout(TIME_OUT);
      outputStream.write("zINSTREAM\0".getBytes(StandardCharsets.UTF_8));
      outputStream.flush();
      byte[] buffer = new byte[BUFFER_SIZE];
      try (InputStream is = socket.getInputStream()) {
        int read = stream.read(buffer);
        while (read >= 0) {
          byte[] chunkSize = ByteBuffer.allocate(4).putInt(read).array();
          outputStream.write(chunkSize);
          outputStream.write(buffer, 0, read);
          if (is.available() > 0) {
            byte[] response = IOUtils.toByteArray(is);
            throw new IOException("Error" + new String(response, StandardCharsets.UTF_8));
          }
          read = stream.read(buffer);
        }
        outputStream.write(new byte[] {0, 0, 0, 0});
        outputStream.flush();
        return is != null
            && parseClamAvResponse(
                IOUtils.toString(is.readAllBytes(), StandardCharsets.UTF_8.name()));
      }
    } catch (IOException e) {
      final String msg = String.format("ClamAV client failure %s", e.getMessage());
      log.error(msg);
      throw new ClamAvServiceException(msg);
    }
  }

  private boolean parseClamAvResponse(final String response) {
    boolean hasVirus = false;
    if (response.contains("OK")) {
      log.info("No Virus detected {}", response);
    } else if (response.contains("FOUND")) {
      hasVirus = true;
      log.info("Virus detected {}", response);
    } else if (response.contains("ERROR")) {
      final String msg = String.format("ClamAv Scan failure %s", response);
      log.error(msg);
      throw new ClamAvServiceException(msg);
    } else {
      final String msg = String.format("ClamAV response unknown %s", response);
      log.error(msg);
      throw new ClamAvServiceException(msg);
    }
    return hasVirus;
  }
}
