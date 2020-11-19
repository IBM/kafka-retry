#
# Copyright 2020 IBM Inc. All rights reserved
# SPDX-License-Identifier: Apache2.0
#

readonly REPO+URL=https://hub.docker.com/repository/docker/ibmcom/kafka-retry
readonly DOCKER_ACCOUNT=ibmcom
readonly REPO_NAME=kafka-retry

if [[ ! -z ${TRAVIS_TAG} ]]; then
  echo "[INFO] Tagging and pushing Docker image to ${REPO_URL} ..."
  docker tag ${REPO_NAME}:${TRAVIS_COMMIT} ${DOCKER_ACCOUNT}/${REPO_NAME}:${TRAVIS_TAG}
  echo ${DOCKER_PASSWORD} | docker login -u ${DOCKER_USERNAME} --password-stdin
  docker push ${DOCKER_ACCOUNT}/${REPO_NAME}:${TRAVIS_TAG}
  echo "[INFO] Image push complete"
else
  echo '[ERROR] Cannot push image as Git tag is empty'
fi
