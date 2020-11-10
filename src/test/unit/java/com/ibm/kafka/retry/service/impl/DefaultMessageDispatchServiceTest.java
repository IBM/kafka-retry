/*
 * Copyright 2020 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */

package com.ibm.kafka.retry.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.kafka.retry.ApplicationProperties;
import com.ibm.kafka.retry.model.MessageHeaders;
import com.ibm.kafka.retry.model.RetryInfo;
import com.ibm.kafka.retry.producer.MessageProducer;
import com.ibm.kafka.retry.repository.MessageRepository;
import com.ibm.kafka.retry.service.MessageDispatchService;
import com.ibm.kafka.retry.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

class DefaultMessageDispatchServiceTest {

    private static final int RETRY_ATTEMPT = 1;

    private static final long SCHEDULE_TIMESTAMP = 2000;

    private static final long DISPATCHABLE_TIMESTAMP = 1000;

    private static final long NON_DISPATCHABLE_TIMESTAMP = 3000;

    private final ApplicationProperties props = mock(ApplicationProperties.class);

    private final MessageRepository repository = mock(MessageRepository.class);

    private final MessageProducer producer = mock(MessageProducer.class);

    private final MessageDispatchService dispatchService = new DefaultMessageDispatchService(props, repository, producer);

    @BeforeEach
    public void setup() {
        Map<Integer, Long> retryDelays = new HashMap<>();
        retryDelays.put(1, TestUtil.FIRST_RETRY_DELAY_MILLIS);
        retryDelays.put(2, TestUtil.SECOND_RETRY_DELAY_MILLIS);
        retryDelays.put(3, TestUtil.THIRD_RETRY_DELAY_MILLIS);
        given(props.getRetryDelaysMs()).willReturn(retryDelays);
    }

    @Test
    @SuppressWarnings("unchecked")
    void nonEmptyQueueWithDispatchableTimeStamp() throws IOException {
        RetryInfo retryInfo = createRetryInfo(DISPATCHABLE_TIMESTAMP);
        JsonNode json = new ObjectMapper().createObjectNode();
        Map<String, RetryInfo> messages = new HashMap<>();
        messages.put(TestUtil.MESSAGE_ID, retryInfo);
        given(repository.getQueuedMessages(RETRY_ATTEMPT)).willReturn(messages);
        dispatchService.dispatchRetries(SCHEDULE_TIMESTAMP);
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        then(producer).should().sendToOriginTopic(eq(retryInfo.getOriginTopic()), captor.capture(), eq(json));
        then(repository).should().removeFromRetryQueue(TestUtil.MESSAGE_ID);
        Map<String, Object> headers = captor.getValue();
        assertEquals(RETRY_ATTEMPT, headers.get(MessageHeaders.RETRY_ATTEMPTS));
    }

    @Test
    @SuppressWarnings("unchecked")
    void nonEmptyQueueWithNonDispatchableTimeStamp() {
        RetryInfo retryInfo = createRetryInfo(NON_DISPATCHABLE_TIMESTAMP);
        JsonNode json = new ObjectMapper().createObjectNode();
        Map<String, RetryInfo> messages = new HashMap<>();
        messages.put(TestUtil.MESSAGE_ID, retryInfo);
        given(repository.getQueuedMessages(RETRY_ATTEMPT)).willReturn(messages);
        dispatchService.dispatchRetries(SCHEDULE_TIMESTAMP);
        then(producer).shouldHaveZeroInteractions();
        then(repository).should(times(0)).removeFromRetryQueue(anyString());
    }

    @Test
    void emptyQueue() {
        given(repository.getQueuedMessages(RETRY_ATTEMPT)).willReturn(Collections.emptyMap());
        dispatchService.dispatchRetries(SCHEDULE_TIMESTAMP);
        then(producer).shouldHaveZeroInteractions();
        then(repository).should(times(0)).removeFromRetryQueue(anyString());
    }

    @Test
    @SuppressWarnings("unchecked")
    void dispatchException() throws IOException {
        RetryInfo retryInfo = createRetryInfo(DISPATCHABLE_TIMESTAMP);
        JsonNode json = new ObjectMapper().createObjectNode();
        Map<String, RetryInfo> messages = new HashMap<>();
        messages.put(TestUtil.MESSAGE_ID, retryInfo);
        given(repository.getQueuedMessages(RETRY_ATTEMPT)).willReturn(messages);
        willThrow(IOException.class).given(producer).sendToOriginTopic(eq(retryInfo.getOriginTopic()), anyMap(), eq(json));
        dispatchService.dispatchRetries(SCHEDULE_TIMESTAMP);
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        then(producer).should().sendToOriginTopic(eq(retryInfo.getOriginTopic()), captor.capture(), eq(json));
        then(repository).should(times(0)).removeFromRetryQueue(anyString());
    }

    private RetryInfo createRetryInfo(long timestamp) {
        return RetryInfo.builder().originTopic(TestUtil.ORIGIN_TOPIC).retryAttempt(RETRY_ATTEMPT).timeStamp(timestamp)
                .payload(new ObjectMapper().createObjectNode()).build();
    }

}
