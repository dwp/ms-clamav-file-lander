package uk.gov.dwp.health.clamav.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MockSocket extends Socket {
  private List<Byte> bytesList = new ArrayList<>();

  public MockSocket() {}

  public InputStream getInputStream() {
    return new ByteArrayInputStream("GET / HTTP/1.1\nHost: localhost".getBytes());
  }

  public OutputStream getOutputStream() {
    return new OutputStream() {
      @Override
      public void write(int b) throws IOException {
        bytesList.add((byte) b);
      }
    };
  }

  public String output() {
    byte[] converted = toByteArray(bytesList);
    return new String(converted, StandardCharsets.UTF_8);
  }

  private byte[] toByteArray(List<Byte> byteList) {
    byte[] byteArray = new byte[byteList.size()];
    int index = 0;
    for (byte b : byteList) {
      byteArray[index++] = b;
    }
    return byteArray;
  }
}
