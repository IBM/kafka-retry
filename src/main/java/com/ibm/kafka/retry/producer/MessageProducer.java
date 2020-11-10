/*
 * Copyright 2020 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */

package com.ibm.kafka.retry.producer;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.common.header.Headers;

import java.io.IOException;
import java.util.Map;

public interface MessageProducer {

    /**
     * Send a message for a retry attempt to the origin topic where it failed.
     * @param topic The origin topic.
     * @param headers New headers to attach to the message.
     * @param payload The message payload.
     * @throws IOException Thrown if the message couldn't be sent.
     */
    void sendToOriginTopic(String topic, Map<String, Object> headers, JsonNode payload) throws IOException;

    /**
     * Send a message to the permanent failure topic.
     * @param headers New headers to attach to the message.
     * @param payload The message payload.
     * @throws IOException Thrown if the message couldn't be sent.
     */
    void sendToPermanentFailureTopic(Headers headers, JsonNode payload) throws IOException;

}
