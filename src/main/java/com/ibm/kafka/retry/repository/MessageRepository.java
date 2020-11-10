/*
 * Copyright 2020 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */

package com.ibm.kafka.retry.repository;

import com.ibm.kafka.retry.model.RetryInfo;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.Map;

public interface MessageRepository {

    /**
     * Initialize the repository. All operations will fail until the repository is initialized.
     * @param messageStore The state store for retriable messages.
     */
    void init(KeyValueStore<String, RetryInfo> messageStore);

    /**
     * Queue a message to a retry attempt.
     * @param messageId The unique message ID.
     * @param retryInfo Reference for the message to retry.
     */
    void addToRetryQueue(String messageId, RetryInfo retryInfo);

    /**
     * Remove a queued message.
     * @param messageId The unique message ID.
     */
    void removeFromRetryQueue(String messageId);

    /**
     * Get the queued retriable messages for dispatch.
     * @param retryAttempt The retry attempt number.
     * @return A map of message references and the original message payload.
     */
    Map<String, RetryInfo> getQueuedMessages(int retryAttempt);

}
