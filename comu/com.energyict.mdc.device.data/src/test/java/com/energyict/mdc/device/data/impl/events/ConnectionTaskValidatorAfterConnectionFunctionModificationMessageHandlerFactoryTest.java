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
 * Tests the {@link ConnectionTaskValidatorAfterConnectionFunctionModificationMessageHandlerFactory} component
 *
 * @author Stijn Vanhoorelbeke
 * @since 18.08.17 - 10:09
 */
@RunWith(MockitoJUnitRunner.class)
public class ConnectionTaskValidatorAfterConnectionFunctionModificationMessageHandlerFactoryTest {

    @Mock
    private MessageService messageService;
    @Mock
    private ServerConnectionTaskService connectionTaskService;

    @Test
    public void test() {
        ConnectionTaskValidatorAfterConnectionFunctionModificationMessageHandlerFactory factory = new ConnectionTaskValidatorAfterConnectionFunctionModificationMessageHandlerFactory(this.messageService, this.connectionTaskService);

        // Business method
        MessageHandler messageHandler = factory.newMessageHandler();

        // Asserts
        assertThat(messageHandler).isNotNull();
        assertThat(messageHandler).isInstanceOf(ConnectionTaskValidatorAfterConnectionFunctionModificationMessageHandler.class);
    }

}
