/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.metering.impl.MeteringInMemoryBootstrapModule;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CachedTypesTest {

    public static final String DEFAULT_READING_TYPE = "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = new MeteringInMemoryBootstrapModule(DEFAULT_READING_TYPE);

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testCachedTypes() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        assertThat(meteringService.getAvailableReadingTypes()).isNotEmpty();
        assertThat(meteringService.getServiceCategory(ServiceKind.HEAT)).isNotNull();
    }
}
