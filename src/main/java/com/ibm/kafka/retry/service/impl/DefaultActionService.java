/*
 * Copyright 2020 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */

package com.ibm.kafka.retry.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.ibm.kafka.retry.exception.IllegalRetryStateException;
import com.ibm.kafka.retry.model.RetryInfo;
import com.ibm.kafka.retry.producer.MessageProducer;
import com.ibm.kafka.retry.repository.MessageRepository;
import com.ibm.kafka.retry.service.ActionService;
import com.ibm.kafka.retry.service.HeaderExtractionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Headers;

import java.io.IOException;

@Slf4j
@AllArgsConstructor
public class DefaultActionService implements ActionService {

    private final HeaderExtractionService headerService;

    private final MessageRepository repository;

    private final MessageProducer producer;

    @Override
    public void queueMessageForRetry(String messageId, Headers headers, JsonNode payload) throws IllegalRetryStateException {
        // TODO validate message
        int numCompletedRetries = headerService.getCompletedRetries(headers);
        log.info("Queueing message {} for retry attempt {}", messageId, numCompletedRetries + 1);
        RetryInfo retryInfo = RetryInfo.builder().payload(payload).timeStamp(headerService.getTimestamp(headers))
                .originTopic(headerService.getOriginTopic(headers)).retryAttempt(numCompletedRetries + 1).build();
        repository.addToRetryQueue(messageId, retryInfo);
    }

    @Override
    public void dispatchPermanentlyFailedMessage(String messageId, Headers headers, JsonNode payload) throws IOException {
        log.warn("Sending message {} to permanent failure topic", messageId);
        producer.sendToPermanentFailureTopic(headers, payload);
    }

    @Override
    public void dropMessage(String messageId) {
        log.warn("Dropping message {}. It will not be retried or sent to permanent failure topic", messageId);
    }

}
