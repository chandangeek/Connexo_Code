/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link PartialConnectionTaskDeletionHandler} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-09 (10:47)
 */
@RunWith(MockitoJUnitRunner.class)
public class PartialConnectionTaskDeletionHandlerTest {

    private static final String TOPIC = "com/energyict/mdc/device/config/partialscheduledconnectiontask/VALIDATE_DELETE";

    @Mock
    private DeviceDataModelService deviceDataModelService;
    @Mock
    private ServerConnectionTaskService connectionTaskService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private PartialConnectionTask partialConnectionTask;
    @Mock
    private LocalEvent event;

    private PartialConnectionTaskDeletionHandler eventHandler;

    @Before
    public void createEvent() {
        EventType eventType = mock(EventType.class);
        when(eventType.getTopic()).thenReturn(TOPIC);
        when(this.event.getSource()).thenReturn(this.partialConnectionTask);
        when(this.event.getType()).thenReturn(eventType);
    }

    @Before
    public void initializeMocks() {
        when(this.deviceConfiguration.getId()).thenReturn(97L);
        when(this.deviceConfiguration.getName()).thenReturn(PartialConnectionTaskDeletionHandlerTest.class.getSimpleName());
        when(this.partialConnectionTask.getId()).thenReturn(101L);
        when(this.partialConnectionTask.getConfiguration()).thenReturn(this.deviceConfiguration);
        when(this.partialConnectionTask.getName()).thenReturn(PartialConnectionTaskDeletionHandlerTest.class.getSimpleName());
    }

    @Before
    public void createEventHandler() {
        when(this.deviceDataModelService.thesaurus()).thenReturn(this.thesaurus);
        when(this.deviceDataModelService.connectionTaskService()).thenReturn(this.connectionTaskService);
        this.eventHandler = new PartialConnectionTaskDeletionHandler(this.deviceDataModelService);
    }

    @Test
    public void testNotUsed() {
        when(this.connectionTaskService.hasConnectionTasks(this.partialConnectionTask)).thenReturn(false);

        // Business method
        this.eventHandler.onEvent(this.event);

        // Asserts
        verify(this.event).getSource();
        verify(this.connectionTaskService).hasConnectionTasks(this.partialConnectionTask);
    }

    @Test(expected = VetoDeletePartialConnectionTaskException.class)
    public void testInUse() {
        when(this.connectionTaskService.hasConnectionTasks(this.partialConnectionTask)).thenReturn(true);

        // Business method
        this.eventHandler.onEvent(this.event);

        // Asserts
        verify(this.event).getSource();
        verify(this.connectionTaskService).hasConnectionTasks(this.partialConnectionTask);
    }

    @Test
    public void testPartialConnectionTaskCreatedTopic() {
        EventType eventType = mock(EventType.class);
        when(eventType.getTopic()).thenReturn("com/energyict/mdc/device/config/partialscheduledconnectiontask/CREATED");
        when(this.event.getSource()).thenReturn(this.partialConnectionTask);
        when(this.event.getType()).thenReturn(eventType);

        // Business method
        this.eventHandler.onEvent(this.event);

        // Asserts
        verify(this.event, never()).getSource();
        verifyZeroInteractions(this.connectionTaskService);
    }

    @Test
    public void testOtherValidateDeleteTopic() {
        EventType eventType = mock(EventType.class);
        when(eventType.getTopic()).thenReturn("com/energyict/mdc/device/config/comtaskenablement/VALIDATEDELETE");
        when(this.event.getSource()).thenReturn(this.partialConnectionTask);
        when(this.event.getType()).thenReturn(eventType);

        // Business method
        this.eventHandler.onEvent(this.event);

        // Asserts
        verify(this.event, never()).getSource();
        verifyZeroInteractions(this.connectionTaskService);
    }

}