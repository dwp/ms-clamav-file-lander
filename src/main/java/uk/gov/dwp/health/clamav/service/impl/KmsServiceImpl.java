package uk.gov.dwp.health.clamav.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.dwp.health.clamav.service.Encryption;
import uk.gov.dwp.health.crypto.CryptoDataManager;
import uk.gov.dwp.health.crypto.CryptoMessage;
import uk.gov.dwp.health.crypto.exception.CryptoException;

import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class KmsServiceImpl implements Encryption<byte[], CryptoMessage> {

  private final CryptoDataManager cryptoDataManager;

  @Override
  public CryptoMessage encrypt(final byte[] content) throws CryptoException {
    log.info("Encrypting file");
    return cryptoDataManager.encrypt(Base64.getEncoder().encodeToString(content));
  }
}
