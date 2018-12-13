/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.filtering;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.events.ComTaskExecutionEvent;
import com.energyict.mdc.engine.events.ConnectionEvent;

import java.util.Arrays;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.events.filtering.ComTaskExecutionFilter} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-31 (15:06)
 */
public class ComTaskExecutionFilterTest {

    @Test
    public void testMatchExpected () {
        ComTaskExecution interestedTask = mock(ComTaskExecution.class);
        when(interestedTask.getId()).thenReturn(44l);
        ComTaskExecution otherTask = mock(ComTaskExecution.class);
        when(otherTask.getId()).thenReturn(55l);
        ComTaskExecutionFilter filter = new ComTaskExecutionFilter(Arrays.asList(interestedTask));
        ComTaskExecutionEvent event = mock(ComTaskExecutionEvent.class);
        when(event.getComTaskExecution()).thenReturn(otherTask);

        // Business method and assert
        assertThat(filter.matches(event)).isTrue();
    }

    @Test
    public void testNoMatchExpected () {
        ComTaskExecution interestedTask = mock(ComTaskExecution.class);
        ComTaskExecutionFilter filter = new ComTaskExecutionFilter(Arrays.asList(interestedTask));
        ComTaskExecutionEvent event = mock(ComTaskExecutionEvent.class);
        when(event.getComTaskExecution()).thenReturn(interestedTask);

        // Business method and assert
        assertThat(filter.matches(event)).isFalse();
    }

    @Test
    public void testNoMatchForConnectionEvents () {
        ComTaskExecution interestedTask = mock(ComTaskExecution.class);
        ComTaskExecutionFilter filter = new ComTaskExecutionFilter(Arrays.asList(interestedTask));
        ConnectionEvent event = mock(ConnectionEvent.class);

        // Business method and assert
        assertThat(filter.matches(event)).isFalse();
    }

    @Test
    public void testConstructor () {
        ComTaskExecution interestedTask = mock(ComTaskExecution.class);

        // Business method
        ComTaskExecutionFilter filter = new ComTaskExecutionFilter(Arrays.asList(interestedTask));

        // Asserts
        assertThat(filter.getComTaskExecutions()).containsOnly(interestedTask);
    }

    @Test
    public void testUpdateComTask () {
        ComTaskExecution interestedTask = mock(ComTaskExecution.class);
        ComTaskExecution otherTask = mock(ComTaskExecution.class);
        ComTaskExecutionFilter filter = new ComTaskExecutionFilter(Arrays.asList(interestedTask));

        // Business method
        filter.setComTaskExecutions(Arrays.asList(otherTask));

        // Asserts
        assertThat(filter.getComTaskExecutions()).containsOnly(otherTask);
    }

}