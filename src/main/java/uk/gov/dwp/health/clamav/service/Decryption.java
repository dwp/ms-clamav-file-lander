package uk.gov.dwp.health.clamav.service;

@FunctionalInterface
public interface Decryption<T, S> {
  S decrypt(T cipher);
}
