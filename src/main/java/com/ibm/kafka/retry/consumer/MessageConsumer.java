/*
 * Copyright 2020 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */

package com.ibm.kafka.retry.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.streams.kstream.KStream;

public interface MessageConsumer {

    /**
     * Consume inbound messages from the retry topic.
     * @param kstream A KStream representing the inbound messages. The key must be the unique message ID.
     */
    void consumeMessage(KStream<String, JsonNode> kstream) throws Exception;

}
