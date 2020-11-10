/*
 * Copyright 2020 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */

package com.ibm.kafka.retry;

import com.ibm.kafka.retry.consumer.MessageConsumer;
import com.ibm.kafka.retry.producer.MessageProducer;
import com.ibm.kafka.retry.repository.MessageRepository;
import com.ibm.kafka.retry.service.ActionService;
import com.ibm.kafka.retry.service.HeaderExtractionService;
import com.ibm.kafka.retry.service.MessageDispatchService;
import com.ibm.kafka.retry.service.MessageQueryService;
import org.apache.kafka.common.serialization.Serde;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.stream.binding.BinderAwareChannelResolver;
import org.springframework.context.ConfigurableApplicationContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Mockito.mock;

class ApplicationConfigurationTest {

    private final ApplicationConfiguration config = new ApplicationConfiguration();

    @Test
    void actionService() {
        assertThat(config.actionService(mock(HeaderExtractionService.class), mock(MessageRepository.class),
                mock(MessageProducer.class)), instanceOf(ActionService.class));
    }

    @Test
    void headerExtractionService() {
        assertThat(config.headerExtractionService(), instanceOf(HeaderExtractionService.class));
    }

    @Test
    void messageDispatchService() {
        assertThat(config.messageDispatchService(mock(ApplicationProperties.class), mock(MessageProducer.class),
                mock(MessageRepository.class)), instanceOf(MessageDispatchService.class));
    }

    @Test
    void messageQueryService() {
        assertThat(config.messageQueryService(mock(ApplicationProperties.class), mock(HeaderExtractionService.class)),
                instanceOf(MessageQueryService.class));
    }

    @Test
    void messageRepository() {
        assertThat(config.messageRepository(), instanceOf(MessageRepository.class));
    }

    @Test
    void messageProducer() {
        assertThat(config.messageProducer(mock(ApplicationProperties.class), mock(BinderAwareChannelResolver.class)),
                instanceOf(MessageProducer.class));
    }

    @Test
    void messageConsumer() {
        assertThat(config.messageConsumer(mock(ConfigurableApplicationContext.class), mock(Serde.class), mock(ApplicationProperties.class),
                mock(MessageQueryService.class), mock(ActionService.class), mock(MessageRepository.class), mock(MessageDispatchService.class)),
                instanceOf(MessageConsumer.class));
    }

    @Test
    void retryInfoListSerde() {
        assertThat(config.retryInfoSerde(), instanceOf(Serde.class));
    }

}
