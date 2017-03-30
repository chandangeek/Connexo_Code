/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.MessageService;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ConnectionTaskValidatorAfterPropertyRemovalMessageHandler} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-16 (15:36)
 */
@RunWith(MockitoJUnitRunner.class)
public class ConnectionTaskValidatorAfterPropertyRemovalMessageHandlerTest {

    @Mock
    private MessageService messageService;
    @Mock
    private ServerConnectionTaskService connectionTaskService;
    @Mock
    private Message message;

    @Test
    public void handlerGetsMessagePayload() {
        ConnectionTaskValidatorAfterPropertyRemovalMessageHandler testInstance = this.testInstance();
        when(this.message.getPayload()).thenReturn("StartConnectionTasksRevalidationAfterPropertyRemoval#1".getBytes());

        // Business method
        testInstance.process(this.message);

        // Asserts
        verify(this.message).getPayload();
    }

    @Test
    public void handlerProcessesConstructedMessage() {
        ConnectionTaskValidatorAfterPropertyRemovalMessageHandler testInstance = this.testInstance();
        when(this.message.getPayload()).thenReturn("StartConnectionTasksRevalidationAfterPropertyRemoval#1".getBytes());

        // Business method
        testInstance.process(this.message);

        // Asserts
        verify(this.connectionTaskService).findConnectionTasksForPartialId(1);
    }

    private ConnectionTaskValidatorAfterPropertyRemovalMessageHandler testInstance() {
        return new ConnectionTaskValidatorAfterPropertyRemovalMessageHandler(this.messageService, this.connectionTaskService);
    }

}