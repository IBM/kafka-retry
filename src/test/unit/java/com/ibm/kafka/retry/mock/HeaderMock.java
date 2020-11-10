/*
 * Copyright 2020 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */

package com.ibm.kafka.retry.mock;

import lombok.AllArgsConstructor;
import org.apache.kafka.common.header.Header;

@AllArgsConstructor
public class HeaderMock implements Header {

    private final String key;

    private final String value;

    @Override
    public String key() {
        return key;
    }

    @Override
    public byte[] value() {
        return value.getBytes();
    }

}
