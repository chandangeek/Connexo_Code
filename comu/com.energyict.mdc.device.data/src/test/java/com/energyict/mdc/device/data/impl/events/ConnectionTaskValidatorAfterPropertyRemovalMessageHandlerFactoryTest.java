/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link ConnectionTaskValidatorAfterPropertyRemovalMessageHandlerFactory} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-16 (14:58)
 */
@RunWith(MockitoJUnitRunner.class)
public class ConnectionTaskValidatorAfterPropertyRemovalMessageHandlerFactoryTest {

    @Mock
    private MessageService messageService;
    @Mock
    private ServerConnectionTaskService connectionTaskService;

    @Test
    public void test() {
        ConnectionTaskValidatorAfterPropertyRemovalMessageHandlerFactory factory = new ConnectionTaskValidatorAfterPropertyRemovalMessageHandlerFactory(this.messageService, this.connectionTaskService);

        // Business method
        MessageHandler messageHandler = factory.newMessageHandler();

        // Asserts
        assertThat(messageHandler).isNotNull();
    }

}