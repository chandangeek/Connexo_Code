/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.filtering;

import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.events.ComPortPoolRelatedEvent;
import com.energyict.mdc.engine.events.ComServerEvent;

import java.util.Arrays;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.events.filtering.ComPortPoolFilter} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-02 (08:40)
 */
public class ComPortPoolFilterTest {

    @Test
    public void testMatchExpected () {
        ComPortPool interestedPool = mock(ComPortPool.class);
        when(interestedPool.getId()).thenReturn(687l);
        ComPortPool otherPool = mock(ComPortPool.class);
        when(otherPool.getId()).thenReturn(617l);
        ComPortPoolFilter filter = new ComPortPoolFilter(Arrays.asList(interestedPool));
        ComPortPoolRelatedEvent event = mock(ComPortPoolRelatedEvent.class);
        when(event.isComPortPoolRelated()).thenReturn(true);
        when(event.getComPortPool()).thenReturn(otherPool);

        // Business method and assert
        assertThat(filter.matches(event)).isTrue();
    }

    @Test
    public void testNoMatchExpected () {
        ComPortPool interestedPool = mock(ComPortPool.class);
        ComPortPoolFilter filter = new ComPortPoolFilter(Arrays.asList(interestedPool));
        ComPortPoolRelatedEvent event = mock(ComPortPoolRelatedEvent.class);
        when(event.isComPortPoolRelated()).thenReturn(true);
        when(event.getComPortPool()).thenReturn(interestedPool);

        // Business method and assert
        assertThat(filter.matches(event)).isFalse();
    }

    @Test
    public void testNoMatchForNonComPortPoolRelatedEvents () {
        ComPortPool interestedPool = mock(ComPortPool.class);
        ComPortPoolFilter filter = new ComPortPoolFilter(Arrays.asList(interestedPool));
        ComServerEvent event = mock(ComServerEvent.class);
        when(event.isComPortPoolRelated()).thenReturn(false);

        // Business method and assert
        assertThat(filter.matches(event)).isFalse();
    }

    @Test
    public void testConstructor () {
        ComPortPool interestedPool = mock(ComPortPool.class);

        // Business method
        ComPortPoolFilter filter = new ComPortPoolFilter(Arrays.asList(interestedPool));

        // Asserts
        assertThat(filter.getComPortPools()).containsOnly(interestedPool);
    }

    @Test
    public void testUpdateComPortPool () {
        ComPortPool interestedPool = mock(ComPortPool.class);
        ComPortPool otherPort = mock(ComPortPool.class);
        ComPortPoolFilter filter = new ComPortPoolFilter(Arrays.asList(interestedPool));

        // Business method
        filter.setComPortPools(Arrays.asList(otherPort));

        // Asserts
        assertThat(filter.getComPortPools()).containsOnly(otherPort);
    }

}