/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.tasks.TaskService;

import org.junit.*;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

/**
 * Tests the {@link CalendarTimeSeriesCacheHandlerFactory} component.
 */
@RunWith(MockitoJUnitRunner.class)
public class CalendarTimeSeriesCacheHandlerFactoryTest {

    @Mock
    private TaskService taskService;
    @Mock
    private MeteringService meteringService;
    @Mock
    private ServerDataAggregationService dataAggregationService;

    @Test
    public void newMessageHandler() {
        CalendarTimeSeriesCacheHandlerFactory instance = this.getInstance();

        // Business method
        MessageHandler messageHandler = instance.newMessageHandler();

        // Asserts
        assertThat(messageHandler).isNotNull();
    }

    private CalendarTimeSeriesCacheHandlerFactory getInstance() {
        return new CalendarTimeSeriesCacheHandlerFactory(this.meteringService, this.dataAggregationService);
    }
}