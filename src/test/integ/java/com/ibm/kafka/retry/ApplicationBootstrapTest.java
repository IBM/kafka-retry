/*
 * Copyright 2020 IBM Inc. All rights reserved
 * SPDX-License-Identifier: Apache2.0
 */

package com.ibm.kafka.retry;

import org.junit.jupiter.api.Test;

public class ApplicationBootstrapTest extends IntegrationTest {
    // Gives code coverage on bootstrap class until integration tests in place
    @Test
    void main() {
        ApplicationBootstrap.main(new String[]{});
    }
}
