/*
 * Copyright 2020 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */

package com.ibm.kafka.retry.service;

import com.ibm.kafka.retry.exception.IllegalRetryStateException;
import org.apache.kafka.common.header.Headers;

public interface MessageQueryService {

    /**
     * @param headers The Kafka Streams message headers.
     * @return True if the message should be queued for retry, false otherwise.
     * @throws IllegalRetryStateException Thrown if the headers are in a bad state.
     */
    boolean isRetriable(Headers headers) throws IllegalRetryStateException;

    /**
     * @param headers The Kafka Streams message headers.
     * @return True if the message should be sent to the permanent failure topic, false otherwise.
     * @throws IllegalRetryStateException Thrown if the headers are in a bad state.
     */
    boolean isPermanentlyFailed(Headers headers) throws IllegalRetryStateException;

    /**
     * @param headers The Kafka Streams message headers.
     * @return True if the message should be dropped, i.e. not queued for retry nor sent to the permanent failure topic,
     * and false otherwise.
     * @throws IllegalRetryStateException Thrown if the headers are in a bad state.
     */
    boolean isDroppable(Headers headers) throws IllegalRetryStateException;

}
