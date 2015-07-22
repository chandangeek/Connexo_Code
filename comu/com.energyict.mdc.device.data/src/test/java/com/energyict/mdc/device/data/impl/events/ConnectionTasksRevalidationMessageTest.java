package com.energyict.mdc.device.data.impl.events;

import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskService;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.MessageService;
import com.google.inject.Injector;

import org.junit.*;
import org.junit.runner.*;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the factory method of the {@link ConnectionTasksRevalidationMessage} class
 * that figures out the class to create from the message contents.
 */
@RunWith(MockitoJUnitRunner.class)
public class ConnectionTasksRevalidationMessageTest {

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
        when(this.message.getPayload()).thenReturn("StartConnectionTasksRevalidationAfterPropertyRemoval#1".getBytes());
        when(this.injector.getInstance(StartConnectionTasksRevalidationAfterPropertyRemoval.class))
                .thenReturn(new StartConnectionTasksRevalidationAfterPropertyRemoval(this.messageService, this.connectionTaskService));

        // Business method
        ConnectionTasksRevalidationMessage.from(this.message, this.injector);

        // Asserts
        verify(this.injector).getInstance(StartConnectionTasksRevalidationAfterPropertyRemoval.class);
    }

    @Test
    public void testRevalidateConnectionTasksAfterPropertyRemoval() {
        when(this.message.getPayload()).thenReturn("RevalidateConnectionTasksAfterPropertyRemoval#1,2,3".getBytes());
        when(this.injector.getInstance(RevalidateConnectionTasksAfterPropertyRemoval.class))
                .thenReturn(new RevalidateConnectionTasksAfterPropertyRemoval(this.messageService, this.connectionTaskService));

        // Business method
        ConnectionTasksRevalidationMessage.from(this.message, this.injector);

        // Asserts
        verify(this.injector).getInstance(RevalidateConnectionTasksAfterPropertyRemoval.class);
    }

}