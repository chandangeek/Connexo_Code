/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.events.FilteringEventReceiverFactoryImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-02 (10:24)
 */
public class FilteringEventReceiverFactoryImplTest {

    @Test
    public void neverProducesNull () {
        FilteringEventReceiverFactoryImpl factory = new FilteringEventReceiverFactoryImpl();

        // Business method
        FilteringEventReceiver filteringEventReceiver = factory.newFor(mock(EventReceiver.class));

        // Asserts
        assertThat(filteringEventReceiver).isNotNull();
    }

    @Test
    public void delegatesToCorrectReceiver () {
        FilteringEventReceiverFactoryImpl factory = new FilteringEventReceiverFactoryImpl();
        EventReceiver eventReceiver = mock(EventReceiver.class);
        EventReceiver otherEventReceiver = mock(EventReceiver.class);

        // Business method
        FilteringEventReceiver filteringEventReceiver = factory.newFor(eventReceiver);

        // Asserts
        assertThat(filteringEventReceiver.delegatesTo(eventReceiver)).isTrue();
        assertThat(filteringEventReceiver.delegatesTo(otherEventReceiver)).isFalse();
    }

}