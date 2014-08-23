#!/bin/bash
# Prepare slug contents
mkdir app
cp -r ./exwhy-web/target/classes ./app/classes
cp -r ./exwhy-web/target/lib ./app/lib
cp -r ${JAVA_HOME}/jre ./app/.jre
mkdir "app/.profile.d" && echo 'export PATH="/app/.jre/bin:$PATH"' >> app/.profile.d/java.sh

# Archive slug
tar czfv slug.tgz ./app
_heroku_deploy_apiKey=`echo ":${HEROKU_API_KEY}" | base64`

# Create slug object
_heroku_deploy_createSlugResponse=$(curl -X POST \
-H "Content-Type: application/json" \
-H "Accept: application/vnd.heroku+json; version=3" \
-H "Authorization: ${_heroku_deploy_apiKey}" \
-d '{"process_types":{"web": "java $JAVA_OPTS -cp ./classes:./lib/* io.hgc.exwhy.web.Application"}}' \
-n https://api.heroku.com/apps/${HEROKU_APP_NAME_STAGE}/slugs)

function _heroku_deploy_parseField {
    echo -ne $2 | grep -o "\"$1\"\s*:\s*\"[^\"]*\"" | head -1 | cut -d '"' -f 4
}
_heroku_deploy_slugS3Url=$(_heroku_deploy_parseField "url" "'${_heroku_deploy_createSlugResponse}'")
_heroku_deploy_slugId=$(_heroku_deploy_parseField "id" "'${_heroku_deploy_createSlugResponse}'")

# Upload archive
curl -X PUT -H "Content-Type:" --data-binary @slug.tgz ${_heroku_deploy_slugS3Url}

# Deploy slug
function deployToHeroku {
    curl -X POST \
    -H "Content-Type: application/json" \
    -H "Accept: application/vnd.heroku+json; version=3" \
    -H "Authorization: ${_heroku_deploy_apiKey}" \
    -d "{\"slug\":\"${_heroku_deploy_slugId}\"}" \
    -n https://api.heroku.com/apps/$1/releases
}

deployToHeroku ${HEROKU_APP_NAME_STAGE}