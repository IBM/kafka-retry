/*
 * Copyright 2020 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */

package com.ibm.kafka.retry;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Data
@ConfigurationProperties(prefix = "com.ibm.kafka.retry")
public class ApplicationProperties {

    public static final String RETRY_CHANNEL = "retryInput";

    private String retriableException;

    private String fatalException;

    private String droppableException;

    private String permanentFailureTopic;

    private String retryInfoStoreName;

    private long retrySchedulerIntervalMs;

    private Map<Integer, Long> retryDelaysMs;

    public int getMaxRetryCount() {
        return retryDelaysMs.size();
    }

}