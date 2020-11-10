/*
 * Copyright 2020 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */

package com.ibm.kafka.retry.service.impl;

import com.ibm.kafka.retry.ApplicationProperties;
import com.ibm.kafka.retry.exception.IllegalRetryStateException;
import com.ibm.kafka.retry.service.HeaderExtractionService;
import com.ibm.kafka.retry.service.MessageQueryService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Headers;

@Slf4j
@AllArgsConstructor
public class DefaultMessageQueryService implements MessageQueryService {

    private final ApplicationProperties props;

    private final HeaderExtractionService headerService;

    @Override
    public boolean isRetriable(Headers headers) throws IllegalRetryStateException {
        boolean exhausted = isRetriesExhausted(headers);
        if (exhausted) {
            log.info("Retry attempts exhausted");
        }
        boolean retriableException = isRetriableException(headers);
        return !exhausted && retriableException;
    }

    @Override
    public boolean isPermanentlyFailed(Headers headers) throws IllegalRetryStateException {
        String exceptionName = headerService.getExceptionName(headers);
        boolean isFatal = isMatchingException(exceptionName, props.getFatalException());
        if (isFatal) {
            log.info("Exception type {} is not retriable", exceptionName);
        }
        return isRetriesExhausted(headers) || isFatal;
    }

    @Override
    public boolean isDroppable(Headers headers) throws IllegalRetryStateException {
        String exceptionName = headerService.getExceptionName(headers);
        boolean fromFailureTopic = props.getPermanentFailureTopic().equals(headerService.getOriginTopic(headers));
        if (fromFailureTopic) {
            log.info("Message is from permanent failure topic");
        }
        boolean matchingException = isMatchingException(exceptionName, props.getDroppableException());
        if (matchingException) {
            log.info("Exception type {} is droppable", exceptionName);
        }
        return fromFailureTopic || matchingException;
    }

    private boolean isRetriableException(Headers headers) throws IllegalRetryStateException {
        String exceptionName = headerService.getExceptionName(headers);
        boolean matches = isMatchingException(exceptionName, props.getRetriableException());
        if (matches) {
            log.info("Exception type {} is retriable", exceptionName);
        }
        return matches;
    }

    private boolean isRetriesExhausted(Headers headers) throws IllegalRetryStateException {
        return headerService.getCompletedRetries(headers) >= props.getMaxRetryCount();
    }

    private boolean isMatchingException(String name, String targetException) {
        return targetException.equals(name);
    }

}
