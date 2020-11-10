/*
 * Copyright 2020 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */

package com.ibm.kafka.retry.consumer.serde;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.kafka.support.serializer.JsonSerializer;

public class MessageSerde extends JsonSerde<JsonNode> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Deserializer<JsonNode> deserializer() {
        return new JsonDeserializer<>(JsonNode.class, objectMapper);
    }

    @Override
    public Serializer<JsonNode> serializer() {
        return new JsonSerializer<>(objectMapper);
    }

}
