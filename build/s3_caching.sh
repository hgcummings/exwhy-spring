#!/bin/bash
dependencyFolder=$HOME/.m2
bucket=$AWS_BUCKET
file="/cached.tar.bz2"
region="us-east-1"

service="s3"
timeStamp=$(date -u +%Y%m%dT%H%M%SZ)
dateStamp=$(date -u +%Y%m%d)
scope=${dateStamp}/${region}/${service}/aws4_request
host="${bucket}.s3.amazonaws.com"
url="http://${host}${file}"

function cacheDependencies {
    mkdir dependencies
    tar -cjf dependencies${file} ${dependencyFolder}
    contentHash=$(cat dependencies${file} | openssl sha256 | trimOpenSslOutput)
    authHeader=$(generateAuthHeaderForMethod "PUT")
    curl -X PUT -T "dependencies${file}" \
      -H "Host: ${bucket}.s3.amazonaws.com" \
      -H "X-amz-content-sha256: ${contentHash}" \
      -H "X-amz-date: ${timeStamp}" \
      -H "Authorization: ${authHeader}" \
      ${url}
}

function getCachedDependencies {
    contentHash="e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
    authHeader=$(generateAuthHeaderForMethod "GET")
    responseCode=$(curl \
      -H "Host: ${bucket}.s3.amazonaws.com" \
      -H "X-amz-content-sha256: ${contentHash}" \
      -H "X-amz-date: ${timeStamp}" \
      -H "Authorization: ${authHeader}" \
      -o .${file} \
      -w "%{http_code}" \
      ${url})
    if test ${responseCode} -ne 200; then
        echo -n "${responseCode} "
        cat .${file}
    else
        tar --no-overwrite-dir -xjf .${file} -C /
    fi
}

function generateAuthHeaderForMethod {
    echo $(generateAuthHeaderForCanonicalRequest \
      "$1\n${file}\n\nhost:${host}\nx-amz-content-sha256:${contentHash}\nx-amz-date:${timeStamp}\n\nhost;x-amz-content-sha256;x-amz-date\n${contentHash}")
}

function generateAuthHeaderForCanonicalRequest {
    hashedRequest=$(echo -en $1 | openssl sha256 | trimOpenSslOutput)
    stringToSign="AWS4-HMAC-SHA256\n${timeStamp}\n${scope}\n${hashedRequest}"
    key=$(echo -n "AWS4${AWS_SECRET_ACCESS_KEY}" | xxd -c 256 -ps)
    signature=$(sha256Hash $(sha256Hash $(sha256Hash $(sha256Hash $(sha256Hash \
      ${key} ${dateStamp}) ${region}) ${service}) "aws4_request") ${stringToSign})
    echo "AWS4-HMAC-SHA256 Credential=$AWS_ACCESS_KEY_ID/${scope},SignedHeaders=host;x-amz-content-sha256;x-amz-date,Signature=${signature}"
}

function sha256Hash {
    echo -en $2 | openssl dgst -sha256 -mac Hmac -macopt hexkey:$1 | trimOpenSslOutput
}

function trimOpenSslOutput {
    cut -d ' ' -f 2 $1
}