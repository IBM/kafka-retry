#
# Copyright 2020 IBM Inc. All rights reserved
# SPDX-License-Identifier: Apache2.0
#

set -e

echo "[INFO] Starting application: ${APP_JAR}"
chown -R "${APP_USER}" "${APP_HOME}"
exec su "${APP_USER}" --preserve-environment --command "java ${JVM_OPTS} -jar ${APP_HOME}/${APP_JAR}"
