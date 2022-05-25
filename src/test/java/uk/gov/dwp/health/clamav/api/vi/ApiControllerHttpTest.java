package uk.gov.dwp.health.clamav.api.vi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.dwp.health.clamav.api.v1.ApiController;
import uk.gov.dwp.health.clamav.openapi.model.ServiceRequestObject;
import uk.gov.dwp.health.clamav.service.impl.ClamAvClientImpl;
import uk.gov.dwp.health.clamav.service.impl.FileUploadImpl;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ContextConfiguration(classes = {ApiController.class, FileUploadImpl.class, ClamAvClientImpl.class})
@WebMvcTest
class ApiControllerHttpTest {

  private static ObjectMapper mapper;
  @Autowired private MockMvc mockMvc;
  @MockBean private FileUploadImpl submission;
  @MockBean private ClamAvClientImpl clamAvClient;

  @BeforeAll
  static void setupSpec() {
    mapper = new ObjectMapper();
  }

  @Test
  void testPostFileAndMeta() throws Exception {
    ServiceRequestObject serviceRequest = new ServiceRequestObject();
    serviceRequest.checksum("check_sum");
    serviceRequest.setId("claim_id");
    serviceRequest.setFileName("test.txt");
    serviceRequest.setSizeKb(1024);
    serviceRequest.setPersist(false);
    final String json = mapper.writeValueAsString(serviceRequest);
    MockMultipartFile file =
        new MockMultipartFile(
            "file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "Test Test".getBytes());
    var meta = new MockMultipartFile("meta", "", "application/json", json.getBytes());
    mockMvc
        .perform(MockMvcRequestBuilders.multipart("/v1/scan/upload").file(file).file(meta))
        .andDo(print())
        .andExpect(status().is(200));
  }
}
