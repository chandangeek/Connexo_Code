/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.scheduling.model.impl;

import com.elster.jupiter.events.LocalEvent;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ComTaskDeletionEventHandler} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-01-21 (16:58)
 */
@RunWith(MockitoJUnitRunner.class)
public class ComTaskDeletionEventHandlerTest {

    @Mock
    private ServerSchedulingService schedulingService;
    @Mock
    private ComTask comTask;
    @Mock
    private LocalEvent event;

    @Before
    public void initializeMocks() {
        when(this.event.getSource()).thenReturn(this.comTask);
    }

    @Test
    public void eventHandlerDelegatesToSchedulingService() {
        ComTaskDeletionEventHandler eventHandler = newEventHandler();

        // Business method
        eventHandler.handle(this.event);

        // Asserts
        verify(this.schedulingService).findComSchedulesUsing(this.comTask);
    }

    @Test(expected = VetoDeleteComTaskException.class)
    public void eventHandlerVetosWhenInUse() {
        ComSchedule comSchedule = mock(ComSchedule.class);
        ComTaskDeletionEventHandler eventHandler = newEventHandler();
        when(this.schedulingService.findComSchedulesUsing(this.comTask)).thenReturn(Arrays.asList(comSchedule));

        // Business method
        eventHandler.handle(this.event);

        // Asserts: see expected exception rule
    }

    private ComTaskDeletionEventHandler newEventHandler() {
        return new ComTaskDeletionEventHandler(this.schedulingService);
    }

}