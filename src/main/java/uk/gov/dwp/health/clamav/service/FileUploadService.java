package uk.gov.dwp.health.clamav.service;

import org.springframework.web.multipart.MultipartFile;
import uk.gov.dwp.health.clamav.openapi.model.FileUploadResponse;
import uk.gov.dwp.health.clamav.openapi.model.ServiceRequestObject;

@FunctionalInterface
public interface FileUploadService {

  FileUploadResponse handleFileLanding(ServiceRequestObject meta, MultipartFile file);
}
