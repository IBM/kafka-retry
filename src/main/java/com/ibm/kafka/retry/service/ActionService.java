/*
 * Copyright 2020 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */

package com.ibm.kafka.retry.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.ibm.kafka.retry.exception.IllegalRetryStateException;
import org.apache.kafka.common.header.Headers;

import java.io.IOException;

/**
 * Provides methods for the high-level actions that the Retry Service can do for a received message.
 */
public interface ActionService {

    /**
     * Queue a message to be retried during the next scheduled retry cycle.
     * @param messageId The unique message ID.
     * @param headers The Kafka Streams message headers.
     * @param payload The message payload.
     * @throws IllegalRetryStateException Thrown if the message could not be queued.
     */
    void queueMessageForRetry(String messageId, Headers headers, JsonNode payload) throws IllegalRetryStateException;

    /**
     * Send a permanently failed message to the permanent failure topic.
     * @param messageId The unique message ID.
     * @param headers The Kafka Streams message headers.
     * @param payload The message payload.
     * @throws IOException Thrown is the message could not be sent.
     */
    void dispatchPermanentlyFailedMessage(String messageId, Headers headers, JsonNode payload) throws IOException;

    /**
     * Drop a message from all further processing. It will not be retried and it will not be sent to the permanent
     * failure topic.
     * @param messageId The unique message ID.
     */
    void dropMessage(String messageId);

}
