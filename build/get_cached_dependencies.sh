#!/bin/bash
bucket="exwhy-dependencies"
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
dateKey=`echo -en ${dateStamp} | openssl sha256 -hmac AWS4${AWS_SECRET_ACCESS_KEY} | cut -d ' ' -f 2`
dateRegionKey=`echo -en ${region} | openssl sha256 -hmac ${dateKey} | cut -d ' ' -f 2`
dateRegionServiceKey=`echo -en ${service} | openssl sha256 -hmac ${dateRegionKey} | cut -d ' ' -f 2`
signingKey=`echo -en aws4_request | openssl sha256 -hmac ${dateRegionServiceKey} | cut -d ' ' -f 2`
signature=`echo -en ${stringToSign} | openssl sha256 -hmac ${signingKey} | cut -d ' ' -f 2`
cd $HOME/.m2
curl \
  -H "Host: ${bucket}.s3.amazonaws.com" \
  -H "X-amz-content-sha256: ${emptyHash}" \
  -H "X-amz-date: ${timeStamp}"\
  -H "Authorization: AWS4-HMAC-SHA256 Credential=$AWS_ACCESS_KEY_ID/${scope},SignedHeaders=host;x-amz-content-sha256;x-amz-date,Signature=${signature}" \
  http://${bucket}.s3.amazonaws.com${file}
tar -xjf cached.tar.bz2
ls