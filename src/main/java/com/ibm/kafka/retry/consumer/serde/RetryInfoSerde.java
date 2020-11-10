/*
 * Copyright 2020 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */

package com.ibm.kafka.retry.consumer.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.kafka.retry.model.RetryInfo;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.kafka.support.serializer.JsonSerializer;

public class RetryInfoSerde extends JsonSerde<RetryInfo> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Deserializer<RetryInfo> deserializer() {
        return new JsonDeserializer<>(RetryInfo.class, objectMapper);
    }

    @Override
    public Serializer<RetryInfo> serializer() {
        return new JsonSerializer<>(objectMapper);
    }

}
