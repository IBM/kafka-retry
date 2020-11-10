/*
 * Copyright 2020 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */

package com.ibm.kafka.retry.service.impl;

import com.ibm.kafka.retry.ApplicationProperties;
import com.ibm.kafka.retry.TestUtil;
import com.ibm.kafka.retry.exception.IllegalRetryStateException;
import com.ibm.kafka.retry.service.HeaderExtractionService;
import com.ibm.kafka.retry.service.MessageQueryService;
import org.apache.kafka.common.header.Headers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class DefaultMessageQueryServiceTest {

    private static final int MAX_ATTEMPTS = 3;

    private final ApplicationProperties props = mock(ApplicationProperties.class);

    private final HeaderExtractionService headerService = mock(HeaderExtractionService.class);

    private final MessageQueryService queryService = new DefaultMessageQueryService(props, headerService);

    @BeforeEach
    void setup() {
        given(props.getMaxRetryCount()).willReturn(MAX_ATTEMPTS);
        given(props.getPermanentFailureTopic()).willReturn(TestUtil.PERMANENT_FAILURE_TOPIC);
        given(props.getRetriableException()).willReturn(TestUtil.RETRIABLE_EXCEPTION_NAME);
        given(props.getFatalException()).willReturn(TestUtil.FATAL_EXCEPTION_NAME);
        given(props.getDroppableException()).willReturn(TestUtil.DROPPABLE_EXCEPTION_NAME);
    }

    @Test
    void isRetriable() throws IllegalRetryStateException {
        Headers headers = mock(Headers.class);
        given(headerService.getCompletedRetries(headers)).willReturn(1);
        given(headerService.getExceptionName(headers)).willReturn(TestUtil.RETRIABLE_EXCEPTION_NAME);
        assertTrue(queryService.isRetriable(headers));
    }

    @Test
    void isNotRetriableBecauseAttemptsExhausted() throws IllegalRetryStateException {
        Headers headers = mock(Headers.class);
        given(headerService.getCompletedRetries(headers)).willReturn(MAX_ATTEMPTS);
        assertFalse(queryService.isRetriable(headers));
    }

    @Test
    void isNotRetriableBecauseExceptionType() throws IllegalRetryStateException {
        Headers headers = mock(Headers.class);
        given(headerService.getCompletedRetries(headers)).willReturn(1);
        given(headerService.getExceptionName(headers)).willReturn(TestUtil.FATAL_EXCEPTION_NAME);
        assertFalse(queryService.isRetriable(headers));
    }

    @Test
    void isNotPermanentlyFailed() throws IllegalRetryStateException {
        Headers headers = mock(Headers.class);
        given(headerService.getCompletedRetries(headers)).willReturn(1);
        given(headerService.getExceptionName(headers)).willReturn(TestUtil.RETRIABLE_EXCEPTION_NAME);
        assertFalse(queryService.isPermanentlyFailed(headers));
    }

    @Test
    void isPermanentlyFailedBecauseAttemptsExhausted() throws IllegalRetryStateException {
        Headers headers = mock(Headers.class);
        given(headerService.getCompletedRetries(headers)).willReturn(MAX_ATTEMPTS);
        assertTrue(queryService.isPermanentlyFailed(headers));
    }

    @Test
    void isPermanentlyFailedBecauseExceptionType() throws IllegalRetryStateException {
        Headers headers = mock(Headers.class);
        given(headerService.getExceptionName(headers)).willReturn(TestUtil.FATAL_EXCEPTION_NAME);
        assertTrue(queryService.isPermanentlyFailed(headers));
    }

    @Test
    void isNotDroppable() throws IllegalRetryStateException {
        Headers headers = mock(Headers.class);
        given(headerService.getOriginTopic(headers)).willReturn(TestUtil.ORIGIN_TOPIC);
        given(headerService.getExceptionName(headers)).willReturn(TestUtil.RETRIABLE_EXCEPTION_NAME);
        assertFalse(queryService.isDroppable(headers));
    }

    @Test
    void isDroppableBecauseOrigin() throws IllegalRetryStateException {
        Headers headers = mock(Headers.class);
        given(headerService.getOriginTopic(headers)).willReturn(TestUtil.PERMANENT_FAILURE_TOPIC);
        assertTrue(queryService.isDroppable(headers));
    }

    @Test
    void isDroppableBecauseExceptionType() throws IllegalRetryStateException {
        Headers headers = mock(Headers.class);
        given(headerService.getOriginTopic(headers)).willReturn(TestUtil.ORIGIN_TOPIC);
        given(headerService.getExceptionName(headers)).willReturn(TestUtil.DROPPABLE_EXCEPTION_NAME);
        assertTrue(queryService.isDroppable(headers));
    }

}
