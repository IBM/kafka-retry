/*
 * Copyright 2020 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */

package com.ibm.kafka.retry.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.kafka.retry.ApplicationProperties;
import com.ibm.kafka.retry.consumer.impl.KafkaStreamsMessageConsumer;
import com.ibm.kafka.retry.consumer.serde.MessageSerde;
import com.ibm.kafka.retry.consumer.serde.RetryInfoSerde;
import com.ibm.kafka.retry.exception.IllegalRetryStateException;
import com.ibm.kafka.retry.model.RetryInfo;
import com.ibm.kafka.retry.repository.MessageRepository;
import com.ibm.kafka.retry.service.ActionService;
import com.ibm.kafka.retry.service.MessageDispatchService;
import com.ibm.kafka.retry.service.MessageQueryService;
import com.ibm.kafka.retry.TestUtil;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.state.Stores;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

class KafkaStreamsMessageConsumerTest {

    private final ConfigurableApplicationContext context = mock(ConfigurableApplicationContext.class);

    private final Serde<RetryInfo> retryInfoSerde = mock(Serde.class);

    private final ApplicationProperties props = mock(ApplicationProperties.class);

    private final MessageQueryService queryService = mock(MessageQueryService.class);

    private final ActionService actionService = mock(ActionService.class);

    private final MessageRepository repository = mock(MessageRepository.class);

    private final MessageDispatchService retryService = mock(MessageDispatchService.class);

    private final MessageConsumer consumer = new KafkaStreamsMessageConsumer(context, retryInfoSerde, props, queryService, actionService,
            repository, retryService);

    private TopologyTestDriver testDriver;

    private ConsumerRecordFactory<String, JsonNode> messageFactory;

    @BeforeEach
    void setup() throws Exception {
        StreamsBuilderFactoryBean factoryBean = mock(StreamsBuilderFactoryBean.class);
        given(context.getBean(anyString(), any(Class.class))).willReturn(factoryBean);
        given(factoryBean.getObject()).willReturn(mock(StreamsBuilder.class));

        given(props.getRetryInfoStoreName()).willReturn(TestUtil.MESSAGE_STORE_NAME);
        given(props.getRetrySchedulerIntervalMs()).willReturn(TestUtil.RETRY_SCHEDULER_INTERVAL_MS);
        Map<Integer, Long> retryDelays = new HashMap<>();
        retryDelays.put(1, TestUtil.FIRST_RETRY_DELAY_MILLIS);
        retryDelays.put(2, TestUtil.SECOND_RETRY_DELAY_MILLIS);
        retryDelays.put(3, TestUtil.THIRD_RETRY_DELAY_MILLIS);
        given(props.getRetryDelaysMs()).willReturn(retryDelays);

        Serde<String> messageKeySerde = Serdes.String();
        Serde<JsonNode> messageValueSerde = new MessageSerde();
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, JsonNode> kstream = builder.stream(ApplicationProperties.RETRY_CHANNEL, Consumed.with(messageKeySerde, messageValueSerde));
        builder.addStateStore(Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(TestUtil.MESSAGE_STORE_NAME), Serdes.String(), new RetryInfoSerde()));
        consumer.consumeMessage(kstream);
        Topology topology = builder.build();
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "test:1234");
        testDriver = new TopologyTestDriver(topology, config);
        messageFactory = new ConsumerRecordFactory<>(messageKeySerde.serializer(), messageValueSerde.serializer());
    }

    @AfterEach
    void shutdown() {
        testDriver.close();
    }

    @Test
    void dropMessage() throws IllegalRetryStateException {
        Headers headers = createHeaders();
        given(queryService.isDroppable(headers)).willReturn(true);
        given(queryService.isPermanentlyFailed(headers)).willReturn(false);
        given(queryService.isRetriable(headers)).willReturn(false);
        sendMessage(TestUtil.MESSAGE_ID, new ObjectMapper().createObjectNode(), headers);
        then(actionService).should().dropMessage(TestUtil.MESSAGE_ID);
        then(actionService).shouldHaveNoMoreInteractions();
    }

    @Test
    void queueMessage() throws IllegalRetryStateException {
        Headers headers = createHeaders();
        given(queryService.isDroppable(headers)).willReturn(false);
        given(queryService.isPermanentlyFailed(headers)).willReturn(false);
        given(queryService.isRetriable(headers)).willReturn(true);
        sendMessage(TestUtil.MESSAGE_ID, new ObjectMapper().createObjectNode(), headers);
        then(actionService).should().queueMessageForRetry(eq(TestUtil.MESSAGE_ID), eq(headers), any(JsonNode.class));
        then(actionService).shouldHaveNoMoreInteractions();
    }

    @Test
    void permanentlyFailMessage() throws IllegalRetryStateException, IOException {
        Headers headers = createHeaders();
        given(queryService.isDroppable(headers)).willReturn(false);
        given(queryService.isPermanentlyFailed(headers)).willReturn(true);
        given(queryService.isRetriable(headers)).willReturn(false);
        JsonNode payload = new ObjectMapper().createObjectNode();
        sendMessage(TestUtil.MESSAGE_ID, payload, headers);
        then(actionService).should().dispatchPermanentlyFailedMessage(TestUtil.MESSAGE_ID, headers, payload);
        then(actionService).shouldHaveNoMoreInteractions();
    }

    @Test
    void unknownScenario() throws IllegalRetryStateException {
        Headers headers = createHeaders();
        given(queryService.isDroppable(headers)).willReturn(false);
        given(queryService.isPermanentlyFailed(headers)).willReturn(false);
        given(queryService.isRetriable(headers)).willReturn(false);
        sendMessage(TestUtil.MESSAGE_ID, new ObjectMapper().createObjectNode(), headers);
        then(actionService).shouldHaveZeroInteractions();
    }

    @Test
    void invokePunctuation() {
        testDriver.advanceWallClockTime(TestUtil.FIRST_RETRY_DELAY_MILLIS);
        then(retryService).should(times(1)).dispatchRetries(anyLong());
    }

    private void sendMessage(String key, JsonNode payload, Headers headers) {
        testDriver.pipeInput(messageFactory.create(ApplicationProperties.RETRY_CHANNEL, key, payload, headers));
    }

    private Headers createHeaders() {
        return new RecordHeaders();
    }

}
