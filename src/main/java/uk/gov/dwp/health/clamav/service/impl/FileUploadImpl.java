package uk.gov.dwp.health.clamav.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.dwp.health.clamav.config.properties.S3ConfigProperties;
import uk.gov.dwp.health.clamav.exception.FailReadUploadFileException;
import uk.gov.dwp.health.clamav.exception.VirusDetectionException;
import uk.gov.dwp.health.clamav.http.pdfconvertor.PdfConvertRequest;
import uk.gov.dwp.health.clamav.openapi.model.FileUploadResponse;
import uk.gov.dwp.health.clamav.openapi.model.ServiceRequestObject;
import uk.gov.dwp.health.clamav.service.ClamAvClientService;
import uk.gov.dwp.health.clamav.service.FileUploadService;
import uk.gov.dwp.health.clamav.utils.FileUtils;

import java.io.IOException;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class FileUploadImpl implements FileUploadService {

  private final ClamAvClientService clamAvClientService;
  private final S3FileWriterImpl s3FileWriter;
  private final S3ConfigProperties s3ConfigProperties;
  private final FileUtils fileUtils;
  private final PdfConvertorClientServiceImpl pdfClientService;

  @Override
  public FileUploadResponse handleFileLanding(ServiceRequestObject meta, MultipartFile file) {
    var resp = new FileUploadResponse();
    try {
      if (!clamAvClientService.scanForVirus(file.getInputStream())) {
        log.info("No virus detected");
        log.info("Original file size [{}] bytes", file.getSize());
        if (Boolean.TRUE.equals(meta.isPersist())) {
          log.info(
              "Persist file {} to S3 bucket {}",
              meta.getFileName(),
              s3ConfigProperties.getBucket());
          fileUtils.validatePasswordProtection(file);
          log.info("File validated no password protection detected");
          final var key = s3FileWriter.write(meta.getId(), file);
          if (Objects.equals(file.getContentType(), MediaType.APPLICATION_PDF_VALUE)) {
            resp.setS3Ref(key);
            resp.setBucket(s3ConfigProperties.getBucket());
            resp.setFileSizeKb(fileUtils.fileSizeInKb((int) file.getSize()));
            log.info("File persisted to S3 key [{}]", key);
          } else {
            var pdfRequest =
                PdfConvertRequest.builder()
                    .id(meta.getId())
                    .fileName(meta.getFileName())
                    .s3Ref(key)
                    .srcBucket(s3ConfigProperties.getBucket())
                    .destBucket(s3ConfigProperties.getBucket())
                    .destEncryptEnabled(s3ConfigProperties.isEncryptEnable())
                    .scrEncryptEnabled(s3ConfigProperties.isEncryptEnable())
                    .build();
            var pdfResponse =
                pdfClientService.postConvertRequest(pdfRequest, file.getContentType());
            log.info(
                "Converted file in [{}] kb", pdfResponse.getFileSizeKb());
            resp.setS3Ref(pdfResponse.getS3Ref());
            resp.setBucket(pdfResponse.getBucketName());
            resp.setFileSizeKb(pdfResponse.getFileSizeKb());
          }
        }
      } else {
        log.warn("User upload file contains virus");
        throw new VirusDetectionException("Virus detected");
      }
    } catch (IOException ex) {
      final String msg = String.format("Failed to read uploaded file %s", ex.getMessage());
      log.error(msg);
      throw new FailReadUploadFileException(msg);
    }
    return resp;
  }
}
