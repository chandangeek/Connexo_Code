/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.filtering;

import com.energyict.mdc.engine.events.ComServerEvent;
import com.energyict.mdc.engine.events.LoggingEvent;
import com.energyict.mdc.engine.impl.logging.LogLevel;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.events.filtering.LogLevelFilter} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-16 (16:19)
 */
public class LogLevelFilterTest {

    @Test
    public void testMatch () {
        LogLevelFilter filter = new LogLevelFilter(LogLevel.INFO);
        LoggingEvent event = mock(LoggingEvent.class);
        when(event.isLoggingRelated()).thenReturn(true);
        when(event.getLogLevel()).thenReturn(LogLevel.DEBUG);

        // Business method and assert
        assertThat(filter.matches(event)).isTrue();
    }

    @Test
    public void testNoMatchForEqualLevel () {
        LogLevelFilter filter = new LogLevelFilter(LogLevel.INFO);
        LoggingEvent event = mock(LoggingEvent.class);
        when(event.isLoggingRelated()).thenReturn(true);
        when(event.getLogLevel()).thenReturn(LogLevel.INFO);

        // Business method and assert
        assertThat(filter.matches(event)).isFalse();
    }

    @Test
    public void testNoMatchForLowerLevel () {
        LogLevelFilter filter = new LogLevelFilter(LogLevel.INFO);
        LoggingEvent event = mock(LoggingEvent.class);
        when(event.isLoggingRelated()).thenReturn(true);
        when(event.getLogLevel()).thenReturn(LogLevel.ERROR);

        // Business method and assert
        assertThat(filter.matches(event)).isFalse();
    }

    @Test
    public void testNoMatchForNonLoggingRelatedEvents () {
        LogLevelFilter filter = new LogLevelFilter(LogLevel.INFO);
        ComServerEvent event = mock(ComServerEvent.class);
        when(event.isLoggingRelated()).thenReturn(false);

        // Business method and assert
        assertThat(filter.matches(event)).isFalse();
    }

    @Test
    public void testConstructor () {
        // Business method
        LogLevelFilter filter = new LogLevelFilter(LogLevel.DEBUG);

        // Asserts
        assertThat(filter.getLogLevel()).isEqualTo(LogLevel.DEBUG);
    }

    @Test
    public void testUpdateLogLevel () {
        LogLevelFilter filter = new LogLevelFilter(LogLevel.DEBUG);

        // Business method
        filter.setLogLevel(LogLevel.INFO);

        // Asserts
        assertThat(filter.getLogLevel()).isEqualTo(LogLevel.INFO);
    }

}