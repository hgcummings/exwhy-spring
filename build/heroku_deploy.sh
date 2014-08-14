#!/bin/bash
# Create slug archive
mkdir app
cp -r ./exwhy-web/target/classes ./app/classes
cp -r ./exwhy-web/target/lib ./app/lib
tar czfv slug.tgz ./app
apiKey=`echo ":${HEROKU_API_KEY}" | base64`

# Create slug object
response=$(curl -X POST \
-H "Content-Type: application/json" \
-H "Accept: application/vnd.heroku+json; version=3" \
-H "Authorization: ${apiKey}" \
-d '{"process_types":{"web": "java $JAVA_OPTS -cp ./classes:./lib/* io.hgc.exwhy.web.Application"}}' \
-n https://api.heroku.com/apps/${APP_NAME}/slugs)

echo ${response}

function parseField {
    echo -ne $2 | grep -o "\"$1\"\s*:\s*\"[^\"]*\"" | head -1 | cut -d '"' -f 4
}
s3Url=$(parseField "url" "'${response}'")
echo ${s3Url}
slugId=$(parseField "id" "'${response}'")
echo ${slugId}

# Upload archive
curl -X PUT -H "Content-Type:" --data-binary @slug.tgz ${s3Url}

# Deploy slug
curl -X POST \
-H "Content-Type: application/json" \
-H "Accept: application/vnd.heroku+json; version=3" \
-H "Authorization: ${apiKey}" \
-d "{\"slug\":\"${slugId}\"}" \
-n https://api.heroku.com/apps/${APP_NAME}/releases