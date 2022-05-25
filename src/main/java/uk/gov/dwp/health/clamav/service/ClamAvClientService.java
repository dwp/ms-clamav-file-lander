package uk.gov.dwp.health.clamav.service;

import java.io.InputStream;

@FunctionalInterface
public interface ClamAvClientService {
  boolean scanForVirus(final InputStream stream);
}
