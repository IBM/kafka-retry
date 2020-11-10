/*
 * Copyright 2020 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */

package com.ibm.kafka.retry.consumer.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.ibm.kafka.retry.ApplicationProperties;
import com.ibm.kafka.retry.exception.IllegalRetryStateException;
import com.ibm.kafka.retry.model.RetryInfo;
import com.ibm.kafka.retry.repository.MessageRepository;
import com.ibm.kafka.retry.service.ActionService;
import com.ibm.kafka.retry.service.MessageDispatchService;
import com.ibm.kafka.retry.service.MessageQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.state.KeyValueStore;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class RetryMessageProcessor implements Processor<String, JsonNode> {

    private final ApplicationProperties props;

    private final MessageQueryService queryService;

    private final ActionService actionService;

    private final MessageRepository repository;

    private final MessageDispatchService retryService;

    private ProcessorContext context;

    @Override
    public void init(ProcessorContext context) {
        this.context = context;
        initStateStores();
        configureRetryScheduler(props.getRetrySchedulerIntervalMs());
    }

    @Override
    public void process(String messageId, JsonNode message) {
        log.info("Processing message {}", messageId);
        Headers headers = context.headers();
        try {
            if (queryService.isDroppable(headers)) {
                actionService.dropMessage(messageId);
            } else if (queryService.isRetriable(headers)) {
                actionService.queueMessageForRetry(messageId, headers, message);
            } else if (queryService.isPermanentlyFailed(headers)) {
                actionService.dispatchPermanentlyFailedMessage(messageId, headers, message);
            } else {
                throw new IllegalRetryStateException("Could not determine if message is retriable, permanently failed or droppable");
            }
        } catch (IllegalRetryStateException | IOException ex) {
            log.error("Failed to handle message {}", messageId, ex);
        }
    }

    @Override
    public void close() {
        // Not needed
    }

    private void configureRetryScheduler(long scheduleMillis) {
        context.schedule(scheduleMillis, PunctuationType.WALL_CLOCK_TIME, timestamp -> {
                initStateStores();
                retryService.dispatchRetries(timestamp);
        });
    }

    @SuppressWarnings("unchecked")
    private void initStateStores() {
        repository.init((KeyValueStore<String, RetryInfo>) context.getStateStore(props.getRetryInfoStoreName()));
    }

}
