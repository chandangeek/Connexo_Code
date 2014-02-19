package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link DeviceProtocolPluggableClassDeletionMessageHandlerFactory} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-19 (15:00)
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceProtocolPluggableClassDeletionMessageHandlerFactoryTest {

    @Mock
    private JsonService jsonService;
    @Mock
    private NlsService nlsService;
    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock
    private ProtocolPluggableService protocolPluggableService;

    @Test
    public void testNewMessageHandlerDoesNotReturnNull () {
        DeviceProtocolPluggableClassDeletionMessageHandlerFactory factory = new DeviceProtocolPluggableClassDeletionMessageHandlerFactory();
        factory.setDeviceConfigurationService(this.deviceConfigurationService);
        factory.setProtocolPluggableService(this.protocolPluggableService);
        factory.setNlsService(this.nlsService);
        factory.setJsonService(this.jsonService);

        // Business method
        MessageHandler messageHandler = factory.newMessageHandler();

        // Asserts
        assertThat(messageHandler).isNotNull();
    }

}