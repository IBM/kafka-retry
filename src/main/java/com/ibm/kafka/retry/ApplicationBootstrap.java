/*
 * Copyright 2020 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */

package com.ibm.kafka.retry;

import com.ibm.kafka.retry.consumer.ChannelBindings;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;

@SpringBootApplication
@EnableBinding({ ChannelBindings.class })
@EnableConfigurationProperties(ApplicationProperties.class)
public class ApplicationBootstrap {

    public static void main(final String[] args) { //NOSONAR Avoid false positive on safe use of args
        SpringApplication.run(ApplicationBootstrap.class, args);
    }

}
