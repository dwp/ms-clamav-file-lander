#!/bin/sh

# standard file scan of various sizes and types
IMAGE_LIST=$(find /clamtest/files -name "file*.*" -exec echo {} \;)
for FILE in $IMAGE_LIST
do

  RETURN_CODE=$(curl -H "Content-Type:multipart/form-data" \
  -s -o /dev/null -w "%{http_code}" \
  -F "file=@${FILE}" \
  -X POST "http://ms-clamav-file-submission-service:8080/v1/scan")

  echo "filename -> $FILE.  Expecting 200 got -> $RETURN_CODE"
  if [ "$RETURN_CODE" != "200" ]; then
    exit 1;
  fi
done

# test a password protected pdf returns 406
RETURN_CODE=$(curl -H "Content-Type:multipart/form-data" \
-s -o /dev/null -w "%{http_code}" \
-F "file=@/clamtest/files/Protected-PDF.pdf" \
-X POST "http://ms-clamav-file-submission-service:8080/v1/scan")

echo "filename -> ./files/Protected-PDF.pdf.  Expecting 406 got -> $RETURN_CODE"
if [ "$RETURN_CODE" != "406" ]; then
  exit 1;
fi

exit 0;
