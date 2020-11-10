/*
 * Copyright 2020 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */

package com.ibm.kafka.retry.service;

public interface MessageDispatchService {

    /**
     * Dispatch all queued messages for the retry attempt to their origin topics.
     * @param timeStamp The time stamp from the Kafka Streams processing context
     */
    void dispatchRetries(long timeStamp);

}
