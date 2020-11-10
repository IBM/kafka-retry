/*
 * Copyright 2020 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */

package com.ibm.kafka.retry.service.impl;

import com.ibm.kafka.retry.TestUtil;
import com.ibm.kafka.retry.exception.IllegalRetryStateException;
import com.ibm.kafka.retry.mock.HeaderMock;
import com.ibm.kafka.retry.model.MessageHeaders;
import com.ibm.kafka.retry.service.HeaderExtractionService;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class DefaultHeaderExtractionServiceTest {

    private static final String UNKNOWN_HEADER_KEY = "testKey";

    private static final String UNKNOWN_HEADER_VALUE = "testValue";

    private final HeaderExtractionService headerService = new DefaultHeaderExtractionService();

    @Test
    void getExceptionNameWhenPresent() throws IllegalRetryStateException {
        List<Header> headers = new ArrayList<>();
        headers.add(new HeaderMock(MessageHeaders.EXCEPTION_TYPE, TestUtil.RETRIABLE_EXCEPTION_NAME));
        assertEquals(TestUtil.RETRIABLE_EXCEPTION_NAME, headerService.getExceptionName(mockHeaders(headers, MessageHeaders.EXCEPTION_TYPE)));
    }

    @Test
    void getExceptionNameWhenMissing() {
        List<Header> headers = new ArrayList<>();
        headers.add(new HeaderMock(UNKNOWN_HEADER_KEY, UNKNOWN_HEADER_VALUE));
        assertThrows(IllegalRetryStateException.class, () -> headerService.getExceptionName(mockHeaders(headers, UNKNOWN_HEADER_KEY)));
    }

    @Test
    void getOriginTopicWhenPresent() throws IllegalRetryStateException {
        List<Header> headers = new ArrayList<>();
        headers.add(new HeaderMock(MessageHeaders.ORIGIN_TOPIC, TestUtil.ORIGIN_TOPIC));
        assertEquals(TestUtil.ORIGIN_TOPIC, headerService.getOriginTopic(mockHeaders(headers, MessageHeaders.ORIGIN_TOPIC)));
    }

    @Test
    void getOriginTopicWhenMissing() {
        List<Header> headers = new ArrayList<>();
        headers.add(new HeaderMock(UNKNOWN_HEADER_KEY, UNKNOWN_HEADER_VALUE));
        assertThrows(IllegalRetryStateException.class, () -> headerService.getOriginTopic(mockHeaders(headers, UNKNOWN_HEADER_KEY)));
    }

    @Test
    void getCompletedRetriesWhenPresent() throws IllegalRetryStateException {
        List<Header> headers = new ArrayList<>();
        headers.add(new HeaderMock(MessageHeaders.RETRY_ATTEMPTS, "3"));
        assertEquals(3, headerService.getCompletedRetries(mockHeaders(headers, MessageHeaders.RETRY_ATTEMPTS)));
    }

    @Test
    void getCompletedRetriesWhenMissing() throws IllegalRetryStateException {
        List<Header> headers = new ArrayList<>();
        headers.add(new HeaderMock(UNKNOWN_HEADER_KEY, UNKNOWN_HEADER_VALUE));
        assertEquals(0, headerService.getCompletedRetries(mockHeaders(headers, UNKNOWN_HEADER_KEY)));
    }

    @Test
    void getHeaderWhenMultipleValues() {
        List<Header> headers = new ArrayList<>();
        headers.add(new HeaderMock(MessageHeaders.EXCEPTION_TYPE, TestUtil.RETRIABLE_EXCEPTION_NAME));
        headers.add(new HeaderMock(MessageHeaders.EXCEPTION_TYPE, TestUtil.RETRIABLE_EXCEPTION_NAME));
        assertThrows(IllegalRetryStateException.class, () -> headerService.getOriginTopic(mockHeaders(headers, MessageHeaders.EXCEPTION_TYPE)));
    }

    private Headers mockHeaders(Iterable<Header> iterable, String key) {
        Headers headers = mock(Headers.class);
        given(headers.headers(key)).willReturn(iterable);
        return headers;
    }

}
