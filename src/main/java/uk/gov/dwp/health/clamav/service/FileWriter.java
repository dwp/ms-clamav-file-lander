package uk.gov.dwp.health.clamav.service;

@FunctionalInterface
public interface FileWriter<I, F, S> {
  S write(I i, F f);
}
