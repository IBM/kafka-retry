/*
 * Copyright 2020 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */

package com.ibm.kafka.retry.service;

import com.ibm.kafka.retry.exception.IllegalRetryStateException;
import org.apache.kafka.common.header.Headers;

public interface HeaderExtractionService {

    /**
     * @param headers The Kafka Streams headers for the message.
     * @return The simple name of the exception that caused the message to be sent for retry.
     * @throws IllegalRetryStateException Thrown if the headers are not in a valid state.
     */
    String getExceptionName(Headers headers) throws IllegalRetryStateException;

    /**
     * @param headers The Kafka Streams headers for the message.
     * @return The name of the topic from which the message was consumed when it failed.
     * @throws IllegalRetryStateException Thrown if the headers are not in a valid state.
     */
    String getOriginTopic(Headers headers) throws IllegalRetryStateException;

    /**
     * @param headers The Kafka Streams headers for the message.
     * @return The number of retry attempts that the message has already had.
     * @throws IllegalRetryStateException Thrown if the headers are not in a valid state.
     */
    int getCompletedRetries(Headers headers) throws IllegalRetryStateException;

    /**
     * @param headers The Kafka Streams headers for the message.
     * @return The timestamp of when the message was adding to the retry topic.
     * @throws IllegalRetryStateException Thrown if the headers are not in a valid state.
     */
    long getTimestamp(Headers headers) throws IllegalRetryStateException;

}
