/*
 * Copyright 2020 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */

package com.ibm.kafka.retry.exception;

import org.slf4j.helpers.MessageFormatter;

public class IllegalRetryStateException extends Exception {

    private final transient Object[] messageVars;

    public IllegalRetryStateException(String message, String... messageVars) {
        super(message);
        this.messageVars = messageVars;
    }

    @Override
    public String getMessage() {
        return MessageFormatter.arrayFormat(super.getMessage(), messageVars).getMessage();
    }

}
