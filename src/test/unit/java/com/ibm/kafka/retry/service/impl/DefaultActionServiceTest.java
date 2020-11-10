/*
 * Copyright 2020 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */

package com.ibm.kafka.retry.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.kafka.retry.exception.IllegalRetryStateException;
import com.ibm.kafka.retry.model.RetryInfo;
import com.ibm.kafka.retry.producer.MessageProducer;
import com.ibm.kafka.retry.repository.MessageRepository;
import com.ibm.kafka.retry.service.ActionService;
import com.ibm.kafka.retry.service.HeaderExtractionService;
import com.ibm.kafka.retry.TestUtil;
import org.apache.kafka.common.header.Headers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

class DefaultActionServiceTest {

    private static final long TIMESTAMP = 1000;

    private final HeaderExtractionService headerService = mock(HeaderExtractionService.class);

    private final MessageRepository repository = mock(MessageRepository.class);

    private final MessageProducer producer = mock(MessageProducer.class);

    private final ActionService actionService = new DefaultActionService(headerService, repository, producer);

    @Test
    void queueMessage() throws IllegalRetryStateException {
        int completedRetries = 1;
        Headers headers = mock(Headers.class);
        JsonNode payload = mock(JsonNode.class);
        given(headerService.getCompletedRetries(headers)).willReturn(completedRetries);
        given(headerService.getOriginTopic(headers)).willReturn(TestUtil.ORIGIN_TOPIC);
        given(headerService.getTimestamp(headers)).willReturn(TIMESTAMP);
        actionService.queueMessageForRetry(TestUtil.MESSAGE_ID, headers, payload);
        ArgumentCaptor<RetryInfo> captor = ArgumentCaptor.forClass(RetryInfo.class);
        then(repository).should().addToRetryQueue(eq(TestUtil.MESSAGE_ID), captor.capture());
        assertEquals(payload, captor.getValue().getPayload());
        assertEquals(TIMESTAMP, captor.getValue().getTimeStamp());
        Assertions.assertEquals(TestUtil.ORIGIN_TOPIC, captor.getValue().getOriginTopic());
    }

    @Test
    void dispatchPermanentlyFailedMessage() throws IllegalRetryStateException, IOException {
        Headers headers = mock(Headers.class);
        JsonNode payload = new ObjectMapper().createObjectNode();
        given(headerService.getOriginTopic(headers)).willReturn(TestUtil.ORIGIN_TOPIC);
        actionService.dispatchPermanentlyFailedMessage(TestUtil.MESSAGE_ID, headers, payload);
        then(producer).should().sendToPermanentFailureTopic(headers, payload);
        then(repository).shouldHaveZeroInteractions();
    }

    @Test
    void dropMessage() {
        actionService.dropMessage(TestUtil.MESSAGE_ID);
        then(repository).shouldHaveZeroInteractions();
        then(producer).shouldHaveZeroInteractions();
    }

}
