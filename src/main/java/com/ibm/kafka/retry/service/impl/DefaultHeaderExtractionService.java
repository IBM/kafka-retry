/*
 * Copyright 2020 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */

package com.ibm.kafka.retry.service.impl;

import com.google.common.collect.Streams;
import com.ibm.kafka.retry.exception.IllegalRetryStateException;
import com.ibm.kafka.retry.model.MessageHeaders;
import com.ibm.kafka.retry.service.HeaderExtractionService;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

import java.util.Optional;
import java.util.stream.Stream;

public class DefaultHeaderExtractionService implements HeaderExtractionService {

    @Override
    public String getExceptionName(Headers headers) throws IllegalRetryStateException {
        return getRequiredHeader(headers, MessageHeaders.EXCEPTION_TYPE);
    }

    @Override
    public String getOriginTopic(Headers headers) throws IllegalRetryStateException {
        return getRequiredHeader(headers, MessageHeaders.ORIGIN_TOPIC);
    }

    @Override
    public int getCompletedRetries(Headers headers) throws IllegalRetryStateException {
        int numCompletedRetries = 0;
        if (getHeaderStream(headers, MessageHeaders.RETRY_ATTEMPTS).count() == 1) {
            Optional<Header> optional = getHeaderStream(headers, MessageHeaders.RETRY_ATTEMPTS).findFirst();
            if (optional.isPresent()) {
                String rawValue = new String(optional.get().value());
                if (StringUtils.isNotBlank(rawValue)) {
                    numCompletedRetries = Integer.parseInt(rawValue);
                }
            }
        }
        return numCompletedRetries;
    }

    @Override
    public long getTimestamp(Headers headers) throws IllegalRetryStateException {
        return Long.parseLong(getRequiredHeader(headers, MessageHeaders.PRODUCED_TIMESTAMP_MS));
    }

    private Stream<Header> getHeaderStream(Headers headers, String key) throws IllegalRetryStateException {
        if (Streams.stream(headers.headers(key)).count() > 1) {
            throw new IllegalRetryStateException("Message has more than one header with key {}", key);
        }
        return Streams.stream(headers.headers(key));
    }

    private String getRequiredHeader(Headers headers, String key) throws IllegalRetryStateException {
        String value;
        if (getHeaderStream(headers, key).count() == 1) {
            Optional<Header> optional = getHeaderStream(headers, key).findFirst();
            if (optional.isPresent()) {
                value = new String(optional.get().value());
            } else {
                throw new IllegalRetryStateException("Message is missing header with key {}", key);
            }
        } else {
            throw new IllegalRetryStateException("Message is missing header with key {}", key);
        }
        return StringUtils.strip(value, "\"");
    }

}
