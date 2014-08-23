#!/bin/bash
_s3_caching_dependencyFolder=$HOME/.m2
_s3_caching_file="cached.tar.bz2"

_s3_caching_timeStamp=$(date -u +%Y%m%dT%H%M%SZ)
_s3_caching_dateStamp=$(date -u +%Y%m%d)
_s3_caching_region="us-east-1"
_s3_caching_service="s3"
_s3_caching_scope=${_s3_caching_dateStamp}/${_s3_caching_region}/${_s3_caching_service}/aws4_request
_s3_caching_host="${AWS_BUCKET}.s3.amazonaws.com"
_s3_caching_credential="${AWS_ACCESS_KEY_ID}/${_s3_caching_scope}"
_s3_caching_key=$(echo -n "AWS4${AWS_SECRET_ACCESS_KEY}" | xxd -c 256 -ps)
_s3_caching_signedHeaders="host;x-amz-content-sha256;x-amz-date"
_s3_caching_url="http://${_s3_caching_host}/${_s3_caching_file}"

function getCachedDependencies {
    if [[ -z $(_s3_caching_diffPomFiles) ]]; then
        echo "pom.xml files unchanged - using cached dependencies"
        _s3_caching_downloadArchive
        _s3_caching_extractDependencies
    fi
}

function cacheDependencies {
    if [[ -n $(_s3_caching_diffPomFiles) ]]; then
        echo "pom.xml files have changed - updating cached dependencies"
        _s3_caching_compressDependencies
        _s3_caching_uploadArchive
    fi
}

function _s3_caching_diffPomFiles {
    git diff ${TRAVIS_COMMIT_RANGE} pom.xml **/pom.xml
}

function _s3_caching_downloadArchive {
    _s3_caching_contentHash="e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
    _s3_caching_authHeader=$(_s3_caching_generateAuthHeaderForHttpMethod "GET")
    _s3_caching_responseCode=$(curl \
      -H "Host: ${_s3_caching_host}" \
      -H "X-amz-content-sha256: ${_s3_caching_contentHash}" \
      -H "X-amz-date: ${_s3_caching_timeStamp}" \
      -H "Authorization: ${_s3_caching_authHeader}" \
      -o ${_s3_caching_file} \
      -w "%{http_code}" \
      ${_s3_caching_url})
}

function _s3_caching_extractDependencies {
    if test ${_s3_caching_responseCode} -ne 200; then
        echo -n "${_s3_caching_responseCode} "
        cat ${_s3_caching_file}
    else
        tar --no-overwrite-dir -xjf ${_s3_caching_file} -C /
    fi
}

function _s3_caching_compressDependencies {
    tar -cjf ${_s3_caching_file} ${_s3_caching_dependencyFolder}
}

function _s3_caching_uploadArchive {
    _s3_caching_contentHash=$(cat ${_s3_caching_file} | openssl sha256 | _s3_caching_trimOpenSslOutput)
    _s3_caching_authHeader=$(_s3_caching_generateAuthHeaderForHttpMethod "PUT")
    curl -X PUT -T "${_s3_caching_file}" \
      -H "Host: ${_s3_caching_host}" \
      -H "X-amz-content-sha256: ${_s3_caching_contentHash}" \
      -H "X-amz-date: ${_s3_caching_timeStamp}" \
      -H "Authorization: ${_s3_caching_authHeader}" \
      ${_s3_caching_url}
}

function _s3_caching_generateAuthHeaderForHttpMethod {
    _s3_caching_canonicalRequest="$1\n/${_s3_caching_file}\n\n"
    _s3_caching_canonicalRequest+="host:${_s3_caching_host}\nx-amz-content-sha256:${_s3_caching_contentHash}\nx-amz-date:${_s3_caching_timeStamp}\n\n"
    _s3_caching_canonicalRequest+="${_s3_caching_signedHeaders}\n${_s3_caching_contentHash}"
    echo $(_s3_caching_generateAuthHeaderForCanonicalRequest ${_s3_caching_canonicalRequest})
}

function _s3_caching_generateAuthHeaderForCanonicalRequest {
    _s3_caching_hashedRequest=$(echo -en $1 | openssl sha256 | _s3_caching_trimOpenSslOutput)
    _s3_caching_stringToSign="AWS4-HMAC-SHA256\n${_s3_caching_timeStamp}\n${_s3_caching_scope}\n${_s3_caching_hashedRequest}"
    _s3_caching_signature=$(_s3_caching_sha256Hash $(_s3_caching_sha256Hash $(_s3_caching_sha256Hash $(_s3_caching_sha256Hash $(_s3_caching_sha256Hash \
      ${_s3_caching_key} ${_s3_caching_dateStamp}) ${_s3_caching_region}) ${_s3_caching_service}) "aws4_request") ${_s3_caching_stringToSign})
    echo "AWS4-HMAC-SHA256 Credential=${_s3_caching_credential},SignedHeaders=${_s3_caching_signedHeaders},Signature=${_s3_caching_signature}"
}

function _s3_caching_sha256Hash {
    echo -en $2 | openssl dgst -sha256 -mac Hmac -macopt hexkey:$1 | _s3_caching_trimOpenSslOutput
}

function _s3_caching_trimOpenSslOutput {
    cut -d ' ' -f 2 $1
}