## File submission micro service

This micro-service handles file submissions from the Citizen. The implementation compress [ClamAV](https://www.clamav.net)
is a free software, cross-platform and open-source antivirus, and a SpringBoot application serving as a bridge service
between ClamAV to an outside container, as well as AWS services.

### Rest API

The service API definition can be found [openapi-spec-api.yaml](api-spec/openapi-spec.yaml)

### Build a docker image with ClamAv and SpringBoot application from scratch

```bash
mvn clean verify
```

```bash
docker build -t <img-tag-name> . 
```

to run the image

```bash
docker run -p 9000:8080 <img-tag-name>
```

### How it works

```bash
curl -v -H "Content-Type:multipart/form-data" 
-F "meta=\"{"id": "123", "fileName": "pom.xml", "mime": "plain/txt", "sizeKb": 128, "checksum": "checksum_string", "persist": false }\";type=application/json" 
-F "file=@{file}" -X POST "http://localhost:8080/v1/scan/upload"
```

### Virus scan only Request and Response

#### Request body for virus scan only. No file persist required

```json
{
  "id": "such as a claim id",
  "fileName": "image.jpg",
  "mime": "image/jpg",
  "sizeKb": 128,
  "checksum": "string",
  "persist": false
}
```

##### Success response body - status 200, no virus detected and file persistence not required

```json
{
  "message": null,
  "s3Ref": null,
  "bucket": null
}
```

##### Fail response body - status 406 (virus detected)

```json
{
  "message": "VIRUS DETECTED/MULTIPART FAIL"
}
```

##### Fail response body - status 406 (Unable to open PDF file, file is password protected)

```json
{
  "message": "PASSWORD PROTECTED"
}
```

##### Fail response body - status 500 (multipart file upload failure), indicate service fail to receive the file from client

```json
{
  "message": "MULTIPART FAIL"
}
```

### Virus scan and persist file Request and Response

#### Request body for virus scan and persist file to a designated S3 bucket

```json
{
  "id": "such as a claim id",
  "fileName": "image.jpg",
  "mime": "image/jpg",
  "sizeKb": 128,
  "checksum": "string",
  "persist": true
}
```

##### Success response body - status 200, no virus detected, and file persisted to a designated S3 bucket

```json
{
  "message": "ALL SUCCESS",
  "s3Ref": "123_PRESCRIPTION.JPG.2020",
  "bucket": "pip-bucket"
}
```

##### Fail response body - status 406 (virus detected) or status 500 (multipart or s3)

```json
{
  "message": "VIRUS DETECTED"
}
```

##### Fail response body - status 500 (multipart file upload failure or s3 upload failure)
examples

```json
{
  "message": "MULTIPART FAIL"
}
```
```json 
{
  "message" : "S3 FAIL"
}
```

### AWS S3

Submitted and virus checked file persisted into an AWS `S3` bucket, if (`persist` flag in the request set to `true`)

Environment variables

```properties
AWS_S3_AWS_REGION=eu-west-2
AWS_S3_ENDPOINT_OVERRIDE=http://localstack:4566
AWS_ACCESS_KEY_ID=accesskey
AWS_S3_BUCKET=bucket-name
AWS_S3_ENCRYPT_ENABLE=true
```

### AWS KMS

File persist in `S3` bucket encrypted
with [Crypto](https://gitlab.com/health-pdu/shared/govuk-data-cryptography)

Environment variables

```properties
AWS_ENCRYPTION_KMS_OVERRIDE=http://ms-mock-kms-service:5678
AWS_ENCRYPTION_DATA_KEY=secretkey
AWS_SECRET_ACCESS_KEY=secret
```

### Virus signature library self-check/update

```bash 
ms-clamav-s3-file-submission-service_1  | Thu Jul  9 13:08:34 2020 -> SelfCheck: Database status OK.
ms-clamav-s3-file-submission-service_1  | Thu Jul  9 13:18:34 2020 -> SelfCheck: Database status OK.
....
```

### Converter service/API external configuration, it is flexible enough to config API based data type a paired API can support

```properties
UK_GOV_DWP_HEALTH_CONVERTER_SERVICES[0]_NAME=test-ms-word-pdf
UK_GOV_DWP_HEALTH_CONVERTER_SERVICES[0]_URI=http://word2pdf
UK_GOV_DWP_HEALTH_CONVERTER_SERVICES[0]_ENDPOINT=/v1/convert/s3
UK_GOV_DWP_HEALTH_CONVERTER_SERVICES[0]_TYPES[0]=application/msword
UK_GOV_DWP_HEALTH_CONVERTER_SERVICES[0]_TYPES[1]=application/vnd.openxmlformats-officedocument.wordprocessingml.document
UK_GOV_DWP_HEALTH_CONVERTER_SERVICES[1]_NAME=test-image-pdf
UK_GOV_DWP_HEALTH_CONVERTER_SERVICES[1]_URI=http://img2pdf
UK_GOV_DWP_HEALTH_CONVERTER_SERVICES[1]_ENDPOINT=/v1/convert/s3
UK_GOV_DWP_HEALTH_CONVERTER_SERVICES[1]_TYPES[0]=image/jpeg
UK_GOV_DWP_HEALTH_CONVERTER_SERVICES[1]_TYPES[1]=image/png
```

### File upload size configuration, the service can apply constraints on size of file/request the service allows through configuration variables

```properties
SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE=10MB    
SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE=11MB # total request size including json metadata
```

usage

```java

// configuration
@Configuration
public class CVServiceConfigProperties {
  //...
  // inject configuration and look up an API detail by supporting MIME type
  @Autowired
  private CVServiceConfigProperties config;

  CVService service = config.findServiceByMimeType("image/jpg");
}
```

# System Requirement

ClamAv requires a minimum of 2GB of RAM to launch safely.

# Initial delay

The service has an initial delay as ClamAv starts up and is possibly checking/updating the virus library.
