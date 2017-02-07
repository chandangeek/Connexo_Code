/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.impl.tasks.ServerCommunicationTaskService;
import com.energyict.mdc.scheduling.model.ComSchedule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ComScheduleObsoleteValidator} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-03 (14:43)
 */
@RunWith(MockitoJUnitRunner.class)
public class ComScheduleObsoleteValidatorTest {

    @Mock
    private DeviceDataModelService deviceDataModelService;
    @Mock
    private ServerCommunicationTaskService communicationTaskService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private ComSchedule comSchedule;
    @Mock
    private LocalEvent event;

    private ComScheduleObsoleteValidator eventHandler;

    @Before
    public void createEvent() {
        EventType eventType = mock(EventType.class);
        when(eventType.getTopic()).thenReturn(ComScheduleObsoleteValidator.TOPIC);
        when(this.event.getSource()).thenReturn(this.comSchedule);
        when(this.event.getType()).thenReturn(eventType);
    }

    @Before
    public void createEventHandler() {
        when(this.deviceDataModelService.thesaurus()).thenReturn(this.thesaurus);
        when(this.deviceDataModelService.communicationTaskService()).thenReturn(this.communicationTaskService);
        this.eventHandler = new ComScheduleObsoleteValidator(this.deviceDataModelService);
    }

    @Test
    public void testNotUsed() {
        when(this.communicationTaskService.hasComTaskExecutions(this.comSchedule)).thenReturn(false);

        // Business method
        this.eventHandler.handle(this.event);

        // Asserts
        verify(this.event).getSource();
        verify(this.communicationTaskService).hasComTaskExecutions(this.comSchedule);
    }

    @Test(expected = VetoObsoleteComScheduleException.class)
    public void testInUse() {
        when(this.communicationTaskService.hasComTaskExecutions(this.comSchedule)).thenReturn(true);

        // Business method
        this.eventHandler.handle(this.event);

        // Asserts
        verify(this.event).getSource();
        verify(this.communicationTaskService).hasComTaskExecutions(this.comSchedule);
    }

}