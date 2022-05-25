package uk.gov.dwp.health.clamav.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import uk.gov.dwp.health.clamav.config.properties.S3ConfigProperties;
import uk.gov.dwp.health.clamav.exception.FileVerificationException;
import uk.gov.dwp.health.clamav.exception.S3FileUploadException;
import uk.gov.dwp.health.clamav.service.FileWriter;
import uk.gov.dwp.health.crypto.exception.CryptoException;
import uk.gov.dwp.health.pip2.common.FilePrefix;
import uk.gov.dwp.health.pip2.common.FileUtils;
import uk.gov.dwp.health.pip2.common.StringUtils;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3FileWriterImpl implements FileWriter<String, MultipartFile, String> {

  private final S3Client s3Client;
  private final S3ConfigProperties properties;
  private final KmsServiceImpl kmsService;
  private final ObjectMapper objectMapper;

  @Override
  public String write(final String id, final MultipartFile file) {
    if (id == null || id.isEmpty()) {
      throw new IllegalArgumentException("An ID associated with file required");
    } else if (file == null) {
      throw new IllegalArgumentException("File is null");
    }
    PutObjectResponse response;
    final String sanitizedFileName =
        StringUtils.cleanUpString(Objects.requireNonNull(file.getOriginalFilename()));
    final String key = FileUtils.generatePip2S3Key(FilePrefix.ORI, id, sanitizedFileName);
    try {
      response =
          s3Client.putObject(
              builder ->
                  builder
                      .bucket(properties.getBucket())
                      .key(key)
                      .contentType(file.getContentType())
                      .build(),
              RequestBody.fromBytes(
                  properties.isEncryptEnable()
                      ? objectMapper.writeValueAsBytes(kmsService.encrypt(file.getBytes()))
                      : file.getBytes()));
      if (Optional.ofNullable(response).isPresent()
          && (response.eTag() == null || Objects.requireNonNull(response).eTag().isEmpty())) {
        final String msg = "Fail to persist file to S3 - no eTag/checksum in response";
        log.error(msg);
        throw new FileVerificationException(msg);
      }
    } catch (AwsServiceException
        | SdkClientException
        | FileVerificationException
        | IOException ex) {
      final String msg =
          String.format(
              "Fail to upload file [%s] of claim %s, to S3 bucket %s due to %s",
              sanitizedFileName, id, properties.getBucket(), ex.getMessage());
      log.error(msg);
      throw new S3FileUploadException(msg);
    } catch (CryptoException e) {
      final String msg =
          String.format("Fail to encrypt upload file before save in S3 %s", e.getMessage());
      log.error(msg);
      throw new S3FileUploadException(msg);
    }
    log.info("File {} persisted", key);
    return key;
  }
}
