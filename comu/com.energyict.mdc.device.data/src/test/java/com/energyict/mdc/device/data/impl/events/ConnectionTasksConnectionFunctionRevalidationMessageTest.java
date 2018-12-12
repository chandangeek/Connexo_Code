/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.MessageService;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskService;

import com.google.inject.Injector;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the factory method of the {@link ConnectionTasksConnectionFunctionRevalidationMessage} class
 * that figures out the class to create from the message contents.
 *
 * @author Stijn Vanhoorelbeke
 * @since 18.08.17 - 10:21
 */
@RunWith(MockitoJUnitRunner.class)
public class ConnectionTasksConnectionFunctionRevalidationMessageTest {

    @Mock
    private MessageService messageService;
    @Mock
    private ServerConnectionTaskService connectionTaskService;
    @Mock
    private Injector injector;
    @Mock
    private Message message;

    @Test
    public void testStartConnectionTasksRevalidationAfterPropertyRemoval() {
        when(this.message.getPayload()).thenReturn("StartConnectionTasksRevalidationAfterConnectionFunctionModification#123;1".getBytes());
        when(this.injector.getInstance(StartConnectionTasksRevalidationAfterConnectionFunctionModification.class))
                .thenReturn(new StartConnectionTasksRevalidationAfterConnectionFunctionModification(this.messageService, this.connectionTaskService));

        // Business method
        ConnectionTasksConnectionFunctionRevalidationMessage message = ConnectionTasksConnectionFunctionRevalidationMessage.from(this.message, this.injector);

        // Asserts
        verify(this.injector).getInstance(StartConnectionTasksRevalidationAfterConnectionFunctionModification.class);
        assertThat(message).isInstanceOf(StartConnectionTasksRevalidationAfterConnectionFunctionModification.class);
        assertThat(((StartConnectionTasksRevalidationAfterConnectionFunctionModification) message).getPartialConnectionTaskId()).isEqualTo(123);
        assertThat(((StartConnectionTasksRevalidationAfterConnectionFunctionModification) message).getPreviousConnectionFunctionId()).isEqualTo(1);
    }

    @Test
    public void testRevalidateConnectionTasksAfterPropertyRemoval() {
        when(this.message.getPayload()).thenReturn("RevalidateConnectionTasksAfterConnectionFunctionModification#1;12,13".getBytes());
        when(this.injector.getInstance(RevalidateConnectionTasksAfterConnectionFunctionModification.class))
                .thenReturn(new RevalidateConnectionTasksAfterConnectionFunctionModification(this.messageService, this.connectionTaskService));

        // Business method
        ConnectionTasksConnectionFunctionRevalidationMessage message = ConnectionTasksConnectionFunctionRevalidationMessage.from(this.message, this.injector);

        // Asserts
        verify(this.injector).getInstance(RevalidateConnectionTasksAfterConnectionFunctionModification.class);
        assertThat(message).isInstanceOf(RevalidateConnectionTasksAfterConnectionFunctionModification.class);
        assertThat(((RevalidateConnectionTasksAfterConnectionFunctionModification) message).getPreviousConnectionFunctionId()).isEqualTo(1);
        assertThat(((RevalidateConnectionTasksAfterConnectionFunctionModification) message).getConnectionTaskIds()).contains(12L, 13L);
    }

}