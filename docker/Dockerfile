#
# Copyright 2020 IBM Inc. All rights reserved
# SPDX-License-Identifier: Apache2.0
#

FROM centos:8

ENV APP_USER=kafka-retry \
    APP_HOME=/opt/ibm/kafka-retry \
    APP_JAR=kafka-retry-*.jar \
    JAVA_HOME=/usr/lib/jvm/adoptopenjdk-11-openj9-jre \
    PATH=${JAVA_HOME}/bin:${PATH}

COPY docker/adoptopenjdk.repo /etc/yum.repos.d/adoptopenjdk.repo
COPY docker/start.sh ${APP_HOME}/
COPY build/libs/${APP_JAR} ${APP_HOME}/

RUN yum update -y \
    && yum install openssl adoptopenjdk-11-openj9-jre -y \
    && yum clean all \
    && rm -rf /var/cache/yum \
    && rm -rf /var/tmp/yum-* \
    && chmod o-w -R ${JAVA_HOME}/legal/ \
    && find /var/log -type f -exec chmod g-wx,o-rwx {} + \
    && mkdir -p /opt/ibm/kafka-retry \
    && useradd --create-home --shell /bin/bash --user-group ${APP_USER} \
    && chmod +x ${APP_HOME}/start.sh

EXPOSE 8080

CMD ["/bin/bash", "-c", "${APP_HOME}/start.sh"]
