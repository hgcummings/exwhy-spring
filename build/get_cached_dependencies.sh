#!/bin/bash
bucket=$AWS_BUCKET
file="/cached.tar.bz2"
region="us-east-1"
service="s3"
timeStamp=`date -u +%Y%m%dT%H%M%SZ`
dateStamp=`date -u +%Y%m%d`
emptyHash="e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
scope=${dateStamp}/${region}/${service}/aws4_request
canonicalRequest="GET\n${file}\n\nhost:${bucket}.s3.amazonaws.com\nx-amz-content-sha256:${emptyHash}\nx-amz-date:${timeStamp}\n\nhost;x-amz-content-sha256;x-amz-date\n${emptyHash}"
hashedRequest=`echo -en ${canonicalRequest} | openssl sha256 | cut -d ' ' -f 2`
stringToSign="AWS4-HMAC-SHA256\n${timeStamp}\n${scope}\n${hashedRequest}"
initialKey=`echo -n "AWS4${AWS_SECRET_ACCESS_KEY}" | xxd -c 256 -ps`
dateKey=`echo -n ${dateStamp} | openssl dgst -sha256 -mac Hmac -macopt hexkey:${initialKey} | cut -d ' ' -f 2`
dateRegionKey=`echo -n ${region} | openssl dgst -sha256 -mac Hmac -macopt hexkey:${dateKey} | cut -d ' ' -f 2`
dateRegionServiceKey=`echo -n ${service} | openssl dgst -sha256 -mac Hmac -macopt hexkey:${dateRegionKey} | cut -d ' ' -f 2`
signingKey=`echo -n "aws4_request" | openssl dgst -sha256 -mac Hmac -macopt hexkey:${dateRegionServiceKey} | cut -d ' ' -f 2`
signature=`echo -en ${stringToSign} | openssl dgst -sha256 -mac Hmac -macopt hexkey:${signingKey} | cut -d ' ' -f 2`
responseCode=$(curl \
  -H "Host: ${bucket}.s3.amazonaws.com" \
  -H "X-amz-content-sha256: ${emptyHash}" \
  -H "X-amz-date: ${timeStamp}" \
  -H "Authorization: AWS4-HMAC-SHA256 Credential=$AWS_ACCESS_KEY_ID/${scope},SignedHeaders=host;x-amz-content-sha256;x-amz-date,Signature=${signature}" \
  -o .${file} \
  -w "%{http_code}" \
  http://${bucket}.s3.amazonaws.com${file})
if test ${responseCode} -ne 200; then
  echo -n "${responseCode} "
  cat .${file}
else
  tar --no-overwrite-dir -xjf .${file} -C /
fi