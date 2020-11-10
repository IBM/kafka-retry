/*
 * Copyright 2020 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */

package com.ibm.kafka.retry.repository.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.kafka.retry.model.RetryInfo;
import com.ibm.kafka.retry.repository.MessageRepository;
import com.ibm.kafka.retry.TestUtil;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;

@SuppressWarnings("unchecked")
class KafkaStreamsMessageRepositoryTest {

    private static final long TIMESTAMP_MS = 159609916400L;

    private static final int RETRY_ATTEMPT = 1;

    private final KeyValueStore<String, RetryInfo> messageStore = mock(KeyValueStore.class);

    private final MessageRepository repository = new KafkaStreamsMessageRepository();

    @BeforeEach
    void setup() {
        repository.init(messageStore);
    }

    @Test
    void messageQueued() {
        RetryInfo retryInfo = createRetryInfo();
        repository.addToRetryQueue(TestUtil.MESSAGE_ID, retryInfo);
        then(messageStore).should().put(TestUtil.MESSAGE_ID, retryInfo);
    }

    @Test
    void messageNotQueuedWhenNotInitialized() {
        RetryInfo retryInfo = createRetryInfo();
        repository.init(null);
        repository.addToRetryQueue(TestUtil.MESSAGE_ID, retryInfo);
        then(messageStore).shouldHaveZeroInteractions();
    }

    @Test
    void messageRemovedFromQueue() {
        repository.removeFromRetryQueue(TestUtil.MESSAGE_ID);
        then(messageStore).should().delete(TestUtil.MESSAGE_ID);
    }

    @Test
    void messageNotRemovedFromQueueWhenNotInitialized() {
        repository.init(null);
        repository.removeFromRetryQueue(TestUtil.MESSAGE_ID);
        then(messageStore).shouldHaveZeroInteractions();
    }

    @Test
    void queuedMessagesRetrieved() {
        RetryInfo retryInfo = createRetryInfo();
        KeyValueIterator<String, RetryInfo> iterator = mock(KeyValueIterator.class);
        doCallRealMethod().when(iterator).forEachRemaining(any(Consumer.class));
        KeyValue<String, RetryInfo> entry = new KeyValue<>(TestUtil.MESSAGE_ID, retryInfo);
        given(iterator.hasNext()).willReturn(true, false);
        given(iterator.next()).willReturn(entry);
        given(messageStore.all()).willReturn(iterator);
        given(messageStore.get(TestUtil.MESSAGE_ID)).willReturn(retryInfo);
        Map<String, RetryInfo> messages = repository.getQueuedMessages(RETRY_ATTEMPT);
        assertEquals(1, messages.size());
        assertEquals(retryInfo, messages.get(TestUtil.MESSAGE_ID));
    }

    @Test
    void queuedMessagesEmpty() {
        RetryInfo retryInfo = createRetryInfo();
        KeyValueIterator<String, RetryInfo> iterator = mock(KeyValueIterator.class);
        KeyValue<String, RetryInfo> entry = new KeyValue<>(TestUtil.MESSAGE_ID, retryInfo);
        doCallRealMethod().when(iterator).forEachRemaining(any(Consumer.class));
        given(iterator.hasNext()).willReturn(false);
        given(messageStore.all()).willReturn(iterator);
        Map<String, RetryInfo> messages = repository.getQueuedMessages(RETRY_ATTEMPT);
        assertEquals(0, messages.size());
    }

    @Test
    void queuedMessagesNotRetrievedWhenNotInitialized() {
        repository.init(null);
        repository.getQueuedMessages(RETRY_ATTEMPT);
        then(messageStore).shouldHaveZeroInteractions();
    }

    private RetryInfo createRetryInfo() {
        return RetryInfo.builder().originTopic(TestUtil.ORIGIN_TOPIC).retryAttempt(RETRY_ATTEMPT).timeStamp(TIMESTAMP_MS)
                .payload(new ObjectMapper().createObjectNode()).build();
    }

}
