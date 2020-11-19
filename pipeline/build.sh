#
# Copyright 2020 IBM Inc. All rights reserved
# SPDX-License-Identifier: Apache2.0
#

set -e

echo '[INFO] Starting Gradle build ...'
./gradlew clean build

echo '[INFO] Starting Docker build ...'
docker build -t kafka-retry:${TRAVIS_COMMIT} -f docker/Dockerfile . \
  --label "git.commit=${TRAVIS_COMMIT}" \
  --label "app.version=${TRAVIS_TAG}"

echo '[INFO] Kafka Retry build complete'
