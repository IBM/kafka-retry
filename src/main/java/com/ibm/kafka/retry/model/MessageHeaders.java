/*
 * Copyright 2020 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */

package com.ibm.kafka.retry.model;

public class MessageHeaders {

    private static final String PREFIX = "x-ibm-retry-";

    public static final String RETRY_ATTEMPTS = PREFIX + "attempts";

    public static final String ORIGIN_TOPIC = PREFIX + "origin-topic";

    public static final String EXCEPTION_TYPE = PREFIX + "exception-type";

    public static final String PRODUCED_TIMESTAMP_MS = PREFIX + "timestamp-ms";

}
