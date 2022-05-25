package uk.gov.dwp.health.clamav.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import uk.gov.dwp.health.clamav.constant.ResponseEnum;
import uk.gov.dwp.health.clamav.exception.ConverterServiceException;
import uk.gov.dwp.health.clamav.exception.FailReadUploadFileException;
import uk.gov.dwp.health.clamav.exception.FilePasswordProtectedException;
import uk.gov.dwp.health.clamav.exception.RequestValidationException;
import uk.gov.dwp.health.clamav.exception.S3FileUploadException;
import uk.gov.dwp.health.clamav.exception.UnknownFileTypeException;
import uk.gov.dwp.health.clamav.exception.VirusDetectionException;
import uk.gov.dwp.health.clamav.openapi.model.FailureResponse;

import javax.validation.ConstraintViolationException;

import static uk.gov.dwp.health.clamav.constant.ResponseEnum.CONVERSION_FAIL;
import static uk.gov.dwp.health.clamav.constant.ResponseEnum.MULTIPART_FAIL;
import static uk.gov.dwp.health.clamav.constant.ResponseEnum.PASSWORD_PROTECTED;
import static uk.gov.dwp.health.clamav.constant.ResponseEnum.S3_FAIL;
import static uk.gov.dwp.health.clamav.constant.ResponseEnum.UNKNOWN;
import static uk.gov.dwp.health.clamav.constant.ResponseEnum.VIRUS_DETECTED;

@SuppressWarnings("PMD.TooManyStaticImports")
@RestControllerAdvice
@Component
public class AppControllerAdvise {

  private static Logger log = LoggerFactory.getLogger(AppControllerAdvise.class);

  @ExceptionHandler(value = {Exception.class})
  public ResponseEntity<FailureResponse> handle500(Exception ex) {
    log.error("Unknown error {}", ex.getMessage());
    return errorResponseWithMessage(UNKNOWN);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<Void> handle405(HttpRequestMethodNotSupportedException ex) {
    log.warn("Request method not allowed {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
  }

  @ExceptionHandler(
      value = {
        ConstraintViolationException.class,
        MethodArgumentNotValidException.class,
        HttpMessageNotReadableException.class,
        IllegalArgumentException.class,
        RequestValidationException.class,
      })
  public ResponseEntity<Void> handle400(Exception ex) {
    log.warn("Request body validation failed {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
  }

  @ExceptionHandler(value = {S3FileUploadException.class})
  public ResponseEntity<FailureResponse> handleS3FileUploadException() {
    return errorResponseWithMessage(S3_FAIL);
  }

  @ExceptionHandler(value = {VirusDetectionException.class})
  public ResponseEntity<FailureResponse> handleVirusDetectionException() {
    return errorResponseWithMessage(VIRUS_DETECTED);
  }

  @ExceptionHandler(value = {FilePasswordProtectedException.class})
  public ResponseEntity<FailureResponse> handleFilePasswordProtectedException() {
    return errorResponseWithMessage(PASSWORD_PROTECTED);
  }

  @ExceptionHandler(value = {FailReadUploadFileException.class})
  public ResponseEntity<FailureResponse> handleMultipartFailReadUploadException() {
    return errorResponseWithMessage(MULTIPART_FAIL);
  }

  @ExceptionHandler(value = {UnknownFileTypeException.class, ConverterServiceException.class})
  public ResponseEntity<FailureResponse> handleConversionException() {
    return errorResponseWithMessage(CONVERSION_FAIL);
  }

  private ResponseEntity<FailureResponse> errorResponseWithMessage(ResponseEnum msgEnum) {
    FailureResponse respBody = new FailureResponse();
    respBody.setMessage(msgEnum.message);
    return ResponseEntity.status(msgEnum.status).body(respBody);
  }
}
