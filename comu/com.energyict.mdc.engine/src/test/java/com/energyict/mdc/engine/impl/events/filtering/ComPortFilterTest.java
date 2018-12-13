/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.filtering;

import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.events.ComPortRelatedEvent;
import com.energyict.mdc.engine.events.ComServerEvent;

import java.util.Arrays;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.events.filtering.ComPortFilter} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-02 (08:40)
 */
public class ComPortFilterTest {

    @Test
    public void testMatchExpected () {
        ComPort interestedPort = mock(ComPort.class);
        when(interestedPort.getId()).thenReturn(99l);
        ComPort otherPort = mock(ComPort.class);
        when(otherPort.getId()).thenReturn(1099l);
        ComPortFilter filter = new ComPortFilter(Arrays.asList(interestedPort));
        ComPortRelatedEvent event = mock(ComPortRelatedEvent.class);
        when(event.isComPortRelated()).thenReturn(true);
        when(event.getComPort()).thenReturn(otherPort);

        // Business method and assert
        assertThat(filter.matches(event)).isTrue();
    }

    @Test
    public void testNoMatchExpected () {
        ComPort interestedPort = mock(ComPort.class);
        ComPortFilter filter = new ComPortFilter(Arrays.asList(interestedPort));
        ComPortRelatedEvent event = mock(ComPortRelatedEvent.class);
        when(event.isComPortRelated()).thenReturn(true);
        when(event.getComPort()).thenReturn(interestedPort);

        // Business method and assert
        assertThat(filter.matches(event)).isFalse();
    }

    @Test
    public void testNoMatchForNonComPortRelatedEvents () {
        ComPort interestedPort = mock(ComPort.class);
        ComPortFilter filter = new ComPortFilter(Arrays.asList(interestedPort));
        ComServerEvent event = mock(ComServerEvent.class);
        when(event.isComPortRelated()).thenReturn(false);

        // Business method and assert
        assertThat(filter.matches(event)).isFalse();
    }

    @Test
    public void testConstructor () {
        ComPort interestedPort = mock(ComPort.class);

        // Business method
        ComPortFilter filter = new ComPortFilter(Arrays.asList(interestedPort));

        // Asserts
        assertThat(filter.getComPorts()).containsOnly(interestedPort);
    }

    @Test
    public void testUpdateComPort () {
        ComPort interestedPort = mock(ComPort.class);
        ComPort otherPort = mock(ComPort.class);
        ComPortFilter filter = new ComPortFilter(Arrays.asList(interestedPort));

        // Business method
        filter.setComPorts(Arrays.asList(otherPort));

        // Asserts
        assertThat(filter.getComPorts()).containsOnly(otherPort);
    }

}