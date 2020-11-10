/*
 * Copyright 2020 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */

package com.ibm.kafka.retry.producer.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.ibm.kafka.retry.ApplicationProperties;
import com.ibm.kafka.retry.producer.MessageProducer;
import lombok.AllArgsConstructor;
import org.apache.kafka.common.header.Headers;
import org.springframework.cloud.stream.binding.BinderAwareChannelResolver;
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class DefaultMessageProducer implements MessageProducer {

    private final ApplicationProperties props;

    private final BinderAwareChannelResolver resolver;

    @Override
    public void sendToOriginTopic(String topic, Map<String, Object> headers, JsonNode payload) throws IOException {
        sendMessage(topic, createMessage(headers, payload));
    }

    @Override
    public void sendToPermanentFailureTopic(Headers headers, JsonNode payload) throws IOException {
        sendMessage(props.getPermanentFailureTopic(), createMessage(convertHeaders(headers), payload));
    }

    private Message<?> createMessage(Map<String, Object> headers, JsonNode payload) {
        MessageBuilder<?> builder = MessageBuilder.withPayload(payload);
        headers.forEach(builder::setHeader);
        builder.setHeader(MessageHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return builder.build();
    }

    private Map<String, Object> convertHeaders(Headers headers) {
        Map<String, Object> headerMap = new HashMap<>();
        headers.forEach(header -> headerMap.put(header.key(), header.value()));
        return headerMap;
    }

    private void sendMessage(String topic, Message<?> message) throws IOException {
        try {
            resolver.resolveDestination(topic).send(message);
        } catch (RuntimeException ex) {
            throw new IOException("Failed to send message to topic " + topic, ex);
        }
    }

}
