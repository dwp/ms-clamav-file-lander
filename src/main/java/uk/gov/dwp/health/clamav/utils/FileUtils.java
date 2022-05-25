package uk.gov.dwp.health.clamav.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;
import uk.gov.dwp.health.clamav.constant.MSWordMediaType;
import uk.gov.dwp.health.clamav.exception.FilePasswordProtectedException;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Slf4j
public class FileUtils {

  private static final int ONE_KB = 1024;

  public int fileSizeInKb(final int size) {
    if (size < 0) {
      throw new IllegalArgumentException("File size input is illegal, must equal or grater than 0");
    }
    float sizeInFloat = Float.parseFloat(String.valueOf(size)) / ONE_KB;
    log.info("File size in KB [{}] kb", sizeInFloat);
    return BigDecimal.valueOf(sizeInFloat).setScale(0, RoundingMode.UP).intValue();
  }

  public void validatePasswordProtection(final MultipartFile file) throws IOException {
    switch (Objects.requireNonNull(file.getContentType())) {
      case MediaType.APPLICATION_PDF_VALUE:
        validatePDF(file.getInputStream());
        break;
      case MSWordMediaType.APPLICATION_MS_DOC:
      case MSWordMediaType.APPLICATION_MS_DOCX:
        validateMSWord(file.getInputStream());
        break;
      default:
        log.info("File type {} not subject for psw/encryption test", file.getContentType());
        break;
    }
  }

  private void validatePDF(final InputStream inputStream) throws IOException {
    try {
      log.debug("Load PDF for password protection test");
      PDDocument.load(inputStream);
      log.debug("Completed PDF password protection test - no protection detected");
    } catch (InvalidPasswordException ex) {
      final var msg = String.format("PDF file is password protected %s", ex.getMessage());
      log.debug(msg);
      throw new FilePasswordProtectedException(ex.getMessage());
    }
  }

  private void validateMSWord(final InputStream inputStream) throws IOException {
    log.debug("Load MS word for password protection test");
    var ctx = new ParseContext();
    var contentHandler = new BodyContentHandler(-1);
    var metadata = new Metadata();
    var parser = new AutoDetectParser(new DefaultDetector());
    try {
      parser.parse(inputStream, contentHandler, metadata, ctx);
      log.debug("Completed MS-Word password protection test - no protection detected");
    } catch (SAXException | TikaException e) {
      final var msg = String.format("MS-word file could be password protected %s", e.getMessage());
      log.debug(msg);
      throw new FilePasswordProtectedException(msg);
    }
  }
}
