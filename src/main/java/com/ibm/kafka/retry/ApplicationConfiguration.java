/*
 * Copyright 2020 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */

package com.ibm.kafka.retry;

import com.ibm.kafka.retry.consumer.MessageConsumer;
import com.ibm.kafka.retry.consumer.impl.KafkaStreamsMessageConsumer;
import com.ibm.kafka.retry.consumer.serde.RetryInfoSerde;
import com.ibm.kafka.retry.model.RetryInfo;
import com.ibm.kafka.retry.producer.MessageProducer;
import com.ibm.kafka.retry.producer.impl.DefaultMessageProducer;
import com.ibm.kafka.retry.repository.MessageRepository;
import com.ibm.kafka.retry.repository.impl.KafkaStreamsMessageRepository;
import com.ibm.kafka.retry.service.ActionService;
import com.ibm.kafka.retry.service.HeaderExtractionService;
import com.ibm.kafka.retry.service.MessageDispatchService;
import com.ibm.kafka.retry.service.MessageQueryService;
import com.ibm.kafka.retry.service.impl.DefaultActionService;
import com.ibm.kafka.retry.service.impl.DefaultHeaderExtractionService;
import com.ibm.kafka.retry.service.impl.DefaultMessageDispatchService;
import com.ibm.kafka.retry.service.impl.DefaultMessageQueryService;
import org.apache.kafka.common.serialization.Serde;
import org.springframework.cloud.stream.binding.BinderAwareChannelResolver;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    @Bean
    MessageConsumer messageConsumer(ConfigurableApplicationContext context, Serde<RetryInfo> retryInfoSerde, ApplicationProperties props,
                                    MessageQueryService queryService, ActionService actionService, MessageRepository repository,
                                    MessageDispatchService dispatchService) {
        return new KafkaStreamsMessageConsumer(context, retryInfoSerde, props, queryService, actionService, repository, dispatchService);
    }

    @Bean
    MessageQueryService messageQueryService(ApplicationProperties props, HeaderExtractionService headerExtractionService) {
        return new DefaultMessageQueryService(props, headerExtractionService);
    }

    @Bean
    ActionService actionService(HeaderExtractionService headerExtractionService, MessageRepository repository, MessageProducer producer) {
        return new DefaultActionService(headerExtractionService, repository, producer);
    }

    @Bean
    HeaderExtractionService headerExtractionService() {
        return new DefaultHeaderExtractionService();
    }

    @Bean
    MessageDispatchService messageDispatchService(ApplicationProperties props, MessageProducer producer, MessageRepository repository) {
        return new DefaultMessageDispatchService(props, repository, producer);
    }

    @Bean
    MessageRepository messageRepository() {
        return new KafkaStreamsMessageRepository();
    }

    @Bean
    MessageProducer messageProducer(ApplicationProperties props, BinderAwareChannelResolver resolver) {
        return new DefaultMessageProducer(props, resolver);
    }

    @Bean
    Serde<RetryInfo> retryInfoSerde() {
        return new RetryInfoSerde();
    }

}

