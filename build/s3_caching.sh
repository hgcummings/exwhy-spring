#!/bin/bash
dependencyFolder=$HOME/.m2
file="/cached.tar.bz2"
tempFolder="dependencies"

timeStamp=$(date -u +%Y%m%dT%H%M%SZ)
dateStamp=$(date -u +%Y%m%d)
region="us-east-1"
service="s3"
scope=${dateStamp}/${region}/${service}/aws4_request
host="${AWS_BUCKET}.s3.amazonaws.com"
credential="${AWS_ACCESS_KEY_ID}/${scope}"
key=$(echo -n "AWS4${AWS_SECRET_ACCESS_KEY}" | xxd -c 256 -ps)
signedHeaders="host;x-amz-content-sha256;x-amz-date"
url="http://${host}${file}"

function getCachedDependencies {
    if [[ -z $(diffPomFiles) ]]; then
        echo "pom.xml files unchanged - using cached dependencies"
        downloadArchive
        extractDependencies
    fi
}

function cacheDependencies {
    if [[ -n $(diffPomFiles) ]]; then
        echo "pom.xml files have changed - updating cached dependencies"
        compressDependencies
        uploadArchive
    fi
}

function diffPomFiles {
    git diff ${TRAVIS_COMMIT_RANGE} pom.xml **/pom.xml
}

function downloadArchive {
    contentHash="e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
    authHeader=$(generateAuthHeaderForHttpMethod "GET")
    responseCode=$(curl \
      -H "Host: ${host}" \
      -H "X-amz-content-sha256: ${contentHash}" \
      -H "X-amz-date: ${timeStamp}" \
      -H "Authorization: ${authHeader}" \
      -o .${file} \
      -w "%{http_code}" \
      ${url})
}

function extractDependencies {
    if test ${responseCode} -ne 200; then
        echo -n "${responseCode} "
        cat .${file}
    else
        tar --no-overwrite-dir -xjf .${file} -C /
    fi
}

function compressDependencies {
    mkdir ${tempFolder}
    tar -cjf ${tempFolder}${file} ${dependencyFolder}
}

function uploadArchive {
    contentHash=$(cat dependencies${file} | openssl sha256 | trimOpenSslOutput)
    authHeader=$(generateAuthHeaderForHttpMethod "PUT")
    curl -X PUT -T "dependencies${file}" \
      -H "Host: ${host}" \
      -H "X-amz-content-sha256: ${contentHash}" \
      -H "X-amz-date: ${timeStamp}" \
      -H "Authorization: ${authHeader}" \
      ${url}
}

function generateAuthHeaderForHttpMethod {
    canonicalRequest="$1\n${file}\n\n"
    canonicalRequest+="host:${host}\nx-amz-content-sha256:${contentHash}\nx-amz-date:${timeStamp}\n\n"
    canonicalRequest+="${signedHeaders}\n${contentHash}"
    echo $(generateAuthHeaderForCanonicalRequest ${canonicalRequest})
}

function generateAuthHeaderForCanonicalRequest {
    hashedRequest=$(echo -en $1 | openssl sha256 | trimOpenSslOutput)
    stringToSign="AWS4-HMAC-SHA256\n${timeStamp}\n${scope}\n${hashedRequest}"
    signature=$(sha256Hash $(sha256Hash $(sha256Hash $(sha256Hash $(sha256Hash \
      ${key} ${dateStamp}) ${region}) ${service}) "aws4_request") ${stringToSign})
    echo "AWS4-HMAC-SHA256 Credential=${credential},SignedHeaders=${signedHeaders},Signature=${signature}"
}

function sha256Hash {
    echo -en $2 | openssl dgst -sha256 -mac Hmac -macopt hexkey:$1 | trimOpenSslOutput
}

function trimOpenSslOutput {
    cut -d ' ' -f 2 $1
}