#!/bin/sh
# shellcheck disable=SC2046
awslocal kms create-alias --alias-name alias/test_key_id --target-key-id $(awslocal kms create-key --tags TagKey=Purpose,TagValue=Test --description "test key" --query 'KeyMetadata.KeyId' --output text)
