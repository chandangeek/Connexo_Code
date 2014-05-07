package com.energyict.mdc.engine.impl.events.filtering;

import com.energyict.mdc.engine.events.ComServerEvent;
import com.energyict.mdc.engine.events.ConnectionTaskRelatedEvent;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.impl.events.filtering.ConnectionTaskFilter;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import org.junit.*;

import java.util.Arrays;

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
        BaseDevice device = mock(BaseDevice.class);
        ConnectionTask interestedTask = mock(ConnectionTask.class);
        when(interestedTask.getDevice()).thenReturn(device);
        ConnectionTask otherTask = mock(ConnectionTask.class);
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
        BaseDevice device = mock(BaseDevice.class);
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
        BaseDevice device = mock(BaseDevice.class);
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