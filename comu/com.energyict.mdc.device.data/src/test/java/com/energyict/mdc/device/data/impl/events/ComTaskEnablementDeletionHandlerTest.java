/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.impl.tasks.ServerCommunicationTaskService;
import com.energyict.mdc.tasks.ComTask;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ComTaskEnablementDeletionHandler} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-29 (08:49)
 */
@RunWith(MockitoJUnitRunner.class)
public class ComTaskEnablementDeletionHandlerTest {

    private static final long DEVICE_CONFIGURATION_ID = 97;
    private static final long COMTASK_ID = DEVICE_CONFIGURATION_ID + 1;
    private static final long COMTASK_ENABLEMENT_ID = COMTASK_ID + 1;

    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private ComTask comTask;
    @Mock
    private ComTaskEnablement comTaskEnablement;
    @Mock
    private DeviceDataModelService deviceDataModelService;
    @Mock
    private ServerCommunicationTaskService communicationTaskService;
    @Mock
    private Thesaurus thesaurus;

    @Before
    public void initializeMocks() {
        when(this.deviceConfiguration.getName()).thenReturn(ComTaskEnablementDeletionHandlerTest.class.getSimpleName());
        when(this.deviceConfiguration.getId()).thenReturn(DEVICE_CONFIGURATION_ID);
        when(this.comTask.getId()).thenReturn(COMTASK_ID);
        when(this.comTask.getName()).thenReturn(ComTaskEnablementDeletionHandlerTest.class.getSimpleName());
        when(this.comTaskEnablement.getId()).thenReturn(COMTASK_ENABLEMENT_ID);
        when(this.comTaskEnablement.getComTask()).thenReturn(this.comTask);
        when(this.comTaskEnablement.getDeviceConfiguration()).thenReturn(this.deviceConfiguration);
        when(this.deviceConfigurationService.findComTaskEnablement(COMTASK_ENABLEMENT_ID)).thenReturn(Optional.of(this.comTaskEnablement));
        when(this.deviceDataModelService.communicationTaskService()).thenReturn(this.communicationTaskService);
        when(this.deviceDataModelService.thesaurus()).thenReturn(this.thesaurus);
    }

    @Test
    public void handlerDelegatesToService() {
        LocalEvent localEvent = mock(LocalEvent.class);
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(ComTaskEnablementDeletionHandler.TOPIC);
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(this.comTaskEnablement);

        // Business method
        this.newHandler().handle(localEvent);

        // Asserts
        verify(this.communicationTaskService).hasComTaskExecutions(this.comTaskEnablement);
    }

    @Test(expected = VetoDeleteComTaskEnablementException.class)
    public void handlerVetosWhenInUse() {
        LocalEvent localEvent = mock(LocalEvent.class);
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(ComTaskEnablementDeletionHandler.TOPIC);
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(this.comTaskEnablement);
        when(this.communicationTaskService.hasComTaskExecutions(this.comTaskEnablement)).thenReturn(true);

        // Business method
        this.newHandler().handle(localEvent);

        // Asserts: see expected exception rule
    }

    private ComTaskEnablementDeletionHandler newHandler() {
        return new ComTaskEnablementDeletionHandler(this.deviceDataModelService);
    }

}