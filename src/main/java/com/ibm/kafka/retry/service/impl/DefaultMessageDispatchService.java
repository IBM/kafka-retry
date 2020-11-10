/*
 * Copyright 2020 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */

package com.ibm.kafka.retry.service.impl;

import com.ibm.kafka.retry.ApplicationProperties;
import com.ibm.kafka.retry.model.MessageHeaders;
import com.ibm.kafka.retry.model.RetryInfo;
import com.ibm.kafka.retry.producer.MessageProducer;
import com.ibm.kafka.retry.repository.MessageRepository;
import com.ibm.kafka.retry.service.MessageDispatchService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@AllArgsConstructor
public class DefaultMessageDispatchService implements MessageDispatchService {

    private final ApplicationProperties props;

    private final MessageRepository repository;

    private final MessageProducer producer;

    @Override
    public void dispatchRetries(long timeStamp) {
        props.getRetryDelaysMs().forEach((retryAttempt, retryDelayMs) -> {
            Map<String, RetryInfo> messages = repository.getQueuedMessages(retryAttempt);
            if (!CollectionUtils.isEmpty(messages)) {
                messages.forEach((id, retryInfo) -> {
                    try {
                        if (timeStamp >= getMinimumDispatchTimeStamp(retryAttempt, retryInfo.getTimeStamp())) {
                            String originTopic = retryInfo.getOriginTopic();
                            log.info("Sending message {} to origin topic {}", id, originTopic);
                            producer.sendToOriginTopic(originTopic, createRetryHeaders(retryAttempt), retryInfo.getPayload());
                            repository.removeFromRetryQueue(id);
                        }
                    } catch (IOException ex) {
                        log.error("Failed to dispatch message {} for retry attempt {}. Will try again on next scheduled dispatch",
                                id, retryAttempt, ex);
                    }
                });
            } else {
                log.info("No messages queued for retry attempt {}", retryAttempt);
            }
        });
    }

    private Map<String, Object> createRetryHeaders(int retryAttempt) {
        Map<String, Object> headers = new HashMap<>();
        headers.put(MessageHeaders.RETRY_ATTEMPTS, retryAttempt);
        return headers;
    }

    private long getMinimumDispatchTimeStamp(int retryAttempt, long messageTimeStamp) {
        long timeStamp;
        if (props.getRetryDelaysMs().containsKey(retryAttempt)) {
            timeStamp = messageTimeStamp + props.getRetryDelaysMs().get(retryAttempt);
        } else {
            timeStamp = messageTimeStamp;
            log.error("Retry attempt {} not enabled", retryAttempt);
        }
        return timeStamp;
    }

}
