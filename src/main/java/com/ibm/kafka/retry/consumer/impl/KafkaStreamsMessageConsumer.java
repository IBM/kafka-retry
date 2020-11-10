/*
 * Copyright 2020 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */

package com.ibm.kafka.retry.consumer.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.ibm.kafka.retry.ApplicationProperties;
import com.ibm.kafka.retry.consumer.MessageConsumer;
import com.ibm.kafka.retry.model.RetryInfo;
import com.ibm.kafka.retry.repository.MessageRepository;
import com.ibm.kafka.retry.service.ActionService;
import com.ibm.kafka.retry.service.MessageDispatchService;
import com.ibm.kafka.retry.service.MessageQueryService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;

@Slf4j
@AllArgsConstructor
public class KafkaStreamsMessageConsumer implements MessageConsumer {

    private final ConfigurableApplicationContext context;

    private final Serde<RetryInfo> retryInfoSerde;

    private final ApplicationProperties props;

    private final MessageQueryService queryService;

    private final ActionService actionService;

    private final MessageRepository repository;

    private final MessageDispatchService retryService;

    @Override
    @StreamListener(target = ApplicationProperties.RETRY_CHANNEL)
    public void consumeMessage(KStream<String, JsonNode> kstream) throws Exception {
        addStateStore();
        kstream.process(() -> new RetryMessageProcessor(props, queryService, actionService, repository, retryService),
                props.getRetryInfoStoreName());
    }

    private void addStateStore() throws Exception {
        StreamsBuilderFactoryBean factoryBean = context.getBean("&stream-builder-consumeMessage", StreamsBuilderFactoryBean.class);
        StoreBuilder storeBuilder = Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(props.getRetryInfoStoreName()), Serdes.String(), retryInfoSerde);
        factoryBean.getObject().addStateStore(storeBuilder);
    }


}
