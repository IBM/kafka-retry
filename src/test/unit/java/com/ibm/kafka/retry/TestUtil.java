/*
 * Copyright 2020 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */

package com.ibm.kafka.retry;

public class TestUtil {

    public static final String MESSAGE_ID = "testId";

    public static final String ORIGIN_TOPIC = "originTopic";

    public static final String RETRIABLE_EXCEPTION_NAME = "ProcessingException";

    public static final String FATAL_EXCEPTION_NAME = "FatalProcessingException";

    public static final String DROPPABLE_EXCEPTION_NAME = "HaltProcessingException";

    public static final String PERMANENT_FAILURE_TOPIC = "permanentFailure";

    public static final String MESSAGE_STORE_NAME = "messageStore";

    public static final long FIRST_RETRY_DELAY_MILLIS = 1000;

    public static final long SECOND_RETRY_DELAY_MILLIS = 3000;

    public static final long THIRD_RETRY_DELAY_MILLIS = 9000;

    public static final long RETRY_SCHEDULER_INTERVAL_MS = 500;

}
