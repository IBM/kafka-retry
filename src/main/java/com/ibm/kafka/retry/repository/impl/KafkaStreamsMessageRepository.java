/*
 * Copyright 2020 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */

package com.ibm.kafka.retry.repository.impl;

import com.ibm.kafka.retry.model.RetryInfo;
import com.ibm.kafka.retry.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class KafkaStreamsMessageRepository implements MessageRepository {

    private KeyValueStore<String, RetryInfo> messageStore;

    @Override
    public void init(KeyValueStore<String, RetryInfo> messageStore) {
        this.messageStore = messageStore;
    }

    @Override
    public Map<String, RetryInfo> getQueuedMessages(int retryAttempt) {
        Map<String, RetryInfo> messages = new HashMap<>();
        if (isInitialized()) {
            try (KeyValueIterator<String, RetryInfo> iterator = messageStore.all()) {
                iterator.forEachRemaining(entry -> {
                    if (entry.value.getRetryAttempt() == retryAttempt) {
                        messages.put(entry.key, entry.value);
                    }
                });
            }
        }
        return messages;
    }

    @Override
    public void addToRetryQueue(String messageId, RetryInfo retryInfo) {
        if (isInitialized()) {
            messageStore.put(messageId, retryInfo);
            messageStore.flush();
        }
    }

    @Override
    public void removeFromRetryQueue(String messageId) {
        if (isInitialized()) {
            messageStore.delete(messageId);
            messageStore.flush();
        }
    }

    private boolean isInitialized() {
        boolean initialized = false;
        if (messageStore != null) {
            initialized = true;
        } else {
            log.error("Cannot perform operation as state stores are not initialized");
        }
        return initialized;
    }

}
