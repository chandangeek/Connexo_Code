/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.protocol.api.ConnectionFunction;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ClearConnectionFunctionOnConnectionTaskEventHandler} event handler
 *
 * @author Stijn Vanhoorelbeke
 * @since 16.08.17 - 13:57
 */
@RunWith(MockitoJUnitRunner.class)
public class ClearConnectionFunctionOnConnectionTaskEventHandlerTest {

    @Mock
    private ServerTopologyService topologyService;

    @Test
    public void handle() throws Exception {
        ClearConnectionFunctionOnConnectionTaskEventHandler handler = new ClearConnectionFunctionOnConnectionTaskEventHandler(topologyService);

        LocalEvent event = mock(LocalEvent.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        Device device = mock(Device.class);
        when(connectionTask.getDevice()).thenReturn(device);
        ConnectionFunction connectionFunction = mock(ConnectionFunction.class);
        when(event.getSource()).thenReturn(Pair.of(connectionTask, connectionFunction));

        // Business method
        handler.handle(event);

        // Asserts
        verify(topologyService).clearConnectionTaskHavingConnectionFunctionOnComTasksInDeviceTopology(device, connectionFunction);
    }

}