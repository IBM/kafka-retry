#
# Copyright 2020 IBM Inc. All rights reserved
# SPDX-License-Identifier: Apache2.0
#

set -e

readonly REPO_URL=https://hub.docker.com/repository/docker/ibmcom/kafka-retry
readonly DOCKER_ACCOUNT=ibmcom
readonly REPO_NAME=kafka-retry
readonly MAJOR=$(echo ${VERSION} | cut -d. -f1)
readonly MINOR=$(echo ${VERSION} | cut -d. -f2)
readonly PATCH=$(echo ${VERSION} | cut -d. -f3)

tag_and_push() {
  local tag=${1}
  docker tag ${REPO_NAME}:${TRAVIS_COMMIT} ${DOCKER_ACCOUNT}/${REPO_NAME}:${tag}
  docker push ${DOCKER_ACCOUNT}/${REPO_NAME}:${tag}
}

if [[ ! -z ${TRAVIS_TAG} ]]; then
  echo "[INFO] Tagging and pushing Docker image to ${REPO_URL} ..."
  echo ${DOCKER_PASSWORD} | docker login -u ${DOCKER_USERNAME} --password-stdin
  tag_and_push latest
  tag_and_push ${MAJOR}.${MINOR}.${PATCH}
  tag_and_push ${MAJOR}.${MINOR}
  tag_and_push ${MAJOR}
  echo "[INFO] Image push complete"
else
  echo '[ERROR] Cannot push image as Git tag is empty'
fi
