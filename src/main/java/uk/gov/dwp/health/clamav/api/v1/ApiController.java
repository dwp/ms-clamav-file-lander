package uk.gov.dwp.health.clamav.api.v1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.dwp.health.clamav.constant.ResponseEnum;
import uk.gov.dwp.health.clamav.exception.FailReadUploadFileException;
import uk.gov.dwp.health.clamav.exception.RequestValidationException;
import uk.gov.dwp.health.clamav.exception.VirusDetectionException;
import uk.gov.dwp.health.clamav.openapi.api.V1Api;
import uk.gov.dwp.health.clamav.openapi.model.FileUploadResponse;
import uk.gov.dwp.health.clamav.openapi.model.ServiceRequestObject;
import uk.gov.dwp.health.clamav.service.ClamAvClientService;
import uk.gov.dwp.health.clamav.service.FileUploadService;

import javax.validation.Validator;
import java.io.IOException;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ApiController implements V1Api {

  private final FileUploadService fileUploadService;
  private final ClamAvClientService clamAvClientService;
  private final Validator validator;

  @Override
  public ResponseEntity<FileUploadResponse> _scanAndUpload(
      final ServiceRequestObject meta, final MultipartFile file) {
    log.info("Scan and persist file");
    if (!isMetaValid(meta)) {
      throw new RequestValidationException("Request meta data failed validation");
    }
    var response = fileUploadService.handleFileLanding(meta, file);
    return ResponseEntity.status(ResponseEnum.SUCCESS.status).body(response);
  }

  @Override
  public ResponseEntity<Void> _scanOnly(MultipartFile file) {
    try {
      log.info("Scan file only");
      if (clamAvClientService.scanForVirus(file.getInputStream())) {
        log.warn("User upload file contains virus");
        throw new VirusDetectionException("Virus detected");
      }
      log.info("Upload scanned and no virus detected");
    } catch (IOException e) {
      final String msg = String.format("Fail to read from multipart file %s", e.getMessage());
      log.error(msg);
      throw new FailReadUploadFileException(msg);
    }
    return ResponseEntity.ok().build();
  }

  @SuppressWarnings("java:S1155")
  private boolean isMetaValid(final ServiceRequestObject meta) {
    return validator.validate(meta).size() == 0;
  }
}
