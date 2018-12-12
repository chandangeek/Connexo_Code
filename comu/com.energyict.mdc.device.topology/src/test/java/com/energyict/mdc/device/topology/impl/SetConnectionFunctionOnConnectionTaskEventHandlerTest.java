/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.events.LocalEvent;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.protocol.api.ConnectionFunction;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link SetConnectionFunctionOnConnectionTaskEventHandler} event handler
 *
 * @author Stijn Vanhoorelbeke
 * @since 16.08.17 - 14:06
 */
@RunWith(MockitoJUnitRunner.class)
public class SetConnectionFunctionOnConnectionTaskEventHandlerTest {

    @Mock
    private ServerTopologyService topologyService;

    @Test
    public void handle() throws Exception {
        SetConnectionFunctionOnConnectionTaskEventHandler handler = new SetConnectionFunctionOnConnectionTaskEventHandler(topologyService);

        LocalEvent event = mock(LocalEvent.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        Device device = mock(Device.class);
        when(connectionTask.getDevice()).thenReturn(device);
        ConnectionFunction connectionFunction = mock(ConnectionFunction.class);
        PartialConnectionTask partialConnectionTask = mock(PartialConnectionTask.class);
        when(partialConnectionTask.getConnectionFunction()).thenReturn(Optional.of(connectionFunction));
        when(connectionTask.getPartialConnectionTask()).thenReturn(partialConnectionTask);
        when(event.getSource()).thenReturn(connectionTask);

        // Business method
        handler.handle(event);

        // Asserts
        verify(topologyService).setOrUpdateConnectionTaskHavingConnectionFunctionOnComTasksInDeviceTopology(device, connectionTask);
    }

}