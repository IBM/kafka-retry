/*
 * Copyright 2020 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */

package com.ibm.kafka.retry.producer.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.kafka.retry.ApplicationProperties;
import com.ibm.kafka.retry.mock.HeaderMock;
import com.ibm.kafka.retry.model.MessageHeaders;
import com.ibm.kafka.retry.producer.MessageProducer;
import com.ibm.kafka.retry.TestUtil;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.cloud.stream.binding.BinderAwareChannelResolver;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;

class DefaultMessageProducerTest {

    private static final String HEADER_KEY = "headerKey";

    private static final String HEADER_VALUE = "headerValue";

    private final ApplicationProperties props = mock(ApplicationProperties.class);

    private final BinderAwareChannelResolver resolver = mock(BinderAwareChannelResolver.class);

    private final MessageProducer messageProducer = new DefaultMessageProducer(props, resolver);

    @Test
    @SuppressWarnings("unchecked")
    void sendMessageToOriginTopic() throws IOException {
        int retryCount = 1;
        Map<String, Object> headers = new HashMap<>();
        headers.put(MessageHeaders.RETRY_ATTEMPTS, retryCount);
        JsonNode payload = new ObjectMapper().createObjectNode();
        MessageChannel channel = mock(MessageChannel.class);
        given(resolver.resolveDestination(TestUtil.ORIGIN_TOPIC)).willReturn(channel);
        messageProducer.sendToOriginTopic(TestUtil.ORIGIN_TOPIC, headers, payload);
        ArgumentCaptor<Message<JsonNode>> captor = ArgumentCaptor.forClass(Message.class);
        then(channel).should().send(captor.capture());
        assertEquals(payload, captor.getValue().getPayload());
        assertEquals(retryCount, captor.getValue().getHeaders().get(MessageHeaders.RETRY_ATTEMPTS));
    }

    @Test
    @SuppressWarnings("unchecked")
    void sendMessageToPermanentFailureTopic() throws IOException {
        List<Header> headersList = new ArrayList<>();
        HeaderMock header = new HeaderMock(HEADER_KEY, HEADER_VALUE);
        headersList.add(header);
        Headers headers = mock(Headers.class);
        doCallRealMethod().when(headers).forEach(any(Consumer.class));
        given(headers.iterator()).willReturn(headersList.iterator());
        JsonNode payload = new ObjectMapper().createObjectNode();
        MessageChannel channel = mock(MessageChannel.class);
        given(props.getPermanentFailureTopic()).willReturn(TestUtil.PERMANENT_FAILURE_TOPIC);
        given(resolver.resolveDestination(TestUtil.PERMANENT_FAILURE_TOPIC)).willReturn(channel);
        messageProducer.sendToPermanentFailureTopic(headers, payload);
        ArgumentCaptor<Message<JsonNode>> captor = ArgumentCaptor.forClass(Message.class);
        then(channel).should().send(captor.capture());
        assertEquals(payload, captor.getValue().getPayload());
        assertArrayEquals(HEADER_VALUE.getBytes(), (byte[]) captor.getValue().getHeaders().get(HEADER_KEY));
    }


}
