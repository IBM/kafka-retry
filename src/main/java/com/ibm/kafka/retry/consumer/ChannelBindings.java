/*
 * Copyright 2020 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */

package com.ibm.kafka.retry.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.ibm.kafka.retry.ApplicationProperties;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.cloud.stream.annotation.Input;

public interface ChannelBindings {

    @Input(ApplicationProperties.RETRY_CHANNEL)
    KStream<String, JsonNode> retryChannel();

}
