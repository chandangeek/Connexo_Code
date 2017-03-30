/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.filtering;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.events.ComServerEvent;
import com.energyict.mdc.engine.events.ConnectionTaskRelatedEvent;

import java.util.Arrays;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.events.filtering.ConnectionTaskFilter} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-31 (15:02)
 */
public class ConnectionTaskFilterTest {

    @Test
    public void testMatchExpected () {
        Device device = mock(Device.class);
        ConnectionTask interestedTask = mock(ConnectionTask.class);
        when(interestedTask.getId()).thenReturn(366l);
        when(interestedTask.getDevice()).thenReturn(device);
        ConnectionTask otherTask = mock(ConnectionTask.class);
        when(otherTask.getId()).thenReturn(888l);
        when(otherTask.getDevice()).thenReturn(device);
        ConnectionTaskFilter filter = new ConnectionTaskFilter(Arrays.asList(interestedTask));
        ConnectionTaskRelatedEvent event = mock(ConnectionTaskRelatedEvent.class);
        when(event.isConnectionTaskRelated()).thenReturn(true);
        when(event.getConnectionTask()).thenReturn(otherTask);

        // Business method and assert
        assertThat(filter.matches(event)).isTrue();
    }

    @Test
    public void testNoMatchExpected () {
        Device device = mock(Device.class);
        ConnectionTask interestedTask = mock(ConnectionTask.class);
        when(interestedTask.getDevice()).thenReturn(device);
        ConnectionTaskFilter filter = new ConnectionTaskFilter(Arrays.asList(interestedTask));
        ConnectionTaskRelatedEvent event = mock(ConnectionTaskRelatedEvent.class);
        when(event.isConnectionTaskRelated()).thenReturn(true);
        when(event.getConnectionTask()).thenReturn(interestedTask);

        // Business method and assert
        assertThat(filter.matches(event)).isFalse();
    }

    @Test
    public void testNoMatchExpectedForNonConnectionTaskEvents () {
        Device device = mock(Device.class);
        ConnectionTask interestedTask = mock(ConnectionTask.class);
        when(interestedTask.getDevice()).thenReturn(device);
        ConnectionTaskFilter filter = new ConnectionTaskFilter(Arrays.asList(interestedTask));
        ComServerEvent event = mock(ComServerEvent.class);
        when(event.isConnectionTaskRelated()).thenReturn(false);

        // Business method and assert
        assertThat(filter.matches(event)).isFalse();
    }

    @Test
    public void testConstructor () {
        ConnectionTask interestedTask = mock(ConnectionTask.class);

        // Business method
        ConnectionTaskFilter filter = new ConnectionTaskFilter(Arrays.asList(interestedTask));

        // Asserts
        assertThat(filter.getConnectionTasks()).containsOnly(interestedTask);
    }

    @Test
    public void testUpdateConnectionTask () {
        ConnectionTask interestedTask = mock(ConnectionTask.class);
        ConnectionTask otherTask = mock(ConnectionTask.class);
        ConnectionTaskFilter filter = new ConnectionTaskFilter(Arrays.asList(interestedTask));

        // Business method
        filter.setConnectionTasks(Arrays.asList(otherTask));

        // Asserts
        assertThat(filter.getConnectionTasks()).containsOnly(otherTask);
    }

}