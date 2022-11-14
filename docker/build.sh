#!/usr/bin/env bash

set -o errexit

JAVA_VER=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | sed '/^1\./s///' | cut -d'.' -f1)
if [ "$JAVA_VER" != "17" ]; then echo "ERROR: use java 17 instead of $JAVA_VER"; exit 1; fi

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
PROJECT_DIR=$(dirname "$SCRIPT_DIR")
DEV_DIR=$(dirname "$PROJECT_DIR")

cd "$PROJECT_DIR" || exit
"./mvnw" clean package
cp "$PROJECT_DIR/target/wiremock-generator-0.0.1-SNAPSHOT.jar" "$SCRIPT_DIR/app.jar"

# this needs to be the one that contains wiremock generator
OPEN_API_DIR=$DEV_DIR/openapi-generator
cd "$OPEN_API_DIR" || exit
GIT_BRANCH=$(git branch --show-current)
if [ "$GIT_BRANCH" != "wiremock" ]; then echo "ERROR: openapi-generator project not on wiremock branch but on $GIT_BRANCH"; exit 1; fi
"./mvnw" -DskipTests clean package
cp "$OPEN_API_DIR/modules/openapi-generator-cli/target/openapi-generator-cli.jar" "$SCRIPT_DIR/openapi-generator-cli.jar"

cd "$SCRIPT_DIR" || exit
docker buildx build --platform linux/amd64,linux/arm64 -t stokpop/wiremock-gen:0.0.1 --push .
#docker build -t stokpop/wiremock-gen:0.0.1 .

echo "removing files"
if [ -f app.jar ]; then rm -v app.jar; fi
if [ -f openapi-generator-cli.jar ]; then rm -v openapi-generator-cli.jar; fi
echo "done"
