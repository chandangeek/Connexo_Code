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
 * Tests the {@link ConnectionTaskValidatorAfterConnectionFunctionModificationMessageHandler} component
 *
 * @author Stijn Vanhoorelbeke
 * @since 18.08.17 - 10:15
 */
@RunWith(MockitoJUnitRunner.class)
public class ConnectionTaskValidatorAfterConnectionFunctionModificationMessageHandlerTest {

    @Mock
    private MessageService messageService;
    @Mock
    private ServerConnectionTaskService connectionTaskService;
    @Mock
    private Message message;

    @Test
    public void handlerGetsMessagePayload() {
        ConnectionTaskValidatorAfterConnectionFunctionModificationMessageHandler testInstance = this.testInstance();
        when(this.message.getPayload()).thenReturn("StartConnectionTasksRevalidationAfterConnectionFunctionModification#123;2".getBytes());

        // Business method
        testInstance.process(this.message);

        // Asserts
        verify(this.message).getPayload();
    }

    @Test
    public void handlerProcessesConstructedMessage() {
        ConnectionTaskValidatorAfterConnectionFunctionModificationMessageHandler testInstance = this.testInstance();
        when(this.message.getPayload()).thenReturn("StartConnectionTasksRevalidationAfterConnectionFunctionModification#123;1".getBytes());

        // Business method
        testInstance.process(this.message);

        // Asserts
        verify(this.connectionTaskService).findConnectionTasksForPartialId(123);
    }

    private ConnectionTaskValidatorAfterConnectionFunctionModificationMessageHandler testInstance() {
        return new ConnectionTaskValidatorAfterConnectionFunctionModificationMessageHandler(this.messageService, this.connectionTaskService);
    }

}