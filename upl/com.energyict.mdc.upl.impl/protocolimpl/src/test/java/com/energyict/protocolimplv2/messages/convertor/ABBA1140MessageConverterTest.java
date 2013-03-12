package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.ServerManager;
import com.energyict.mdc.messages.DeviceMessageSpecFactoryImpl;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimpl.iec1107.abba1140.ABBA1140;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ABBA1140MessageConverter} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 16:46
 */
@RunWith(MockitoJUnitRunner.class)
public class ABBA1140MessageConverterTest {

    @Mock
    private ServerManager serverManager;

    @Before
    public void beforeEachTest() {
        when(serverManager.getDeviceMessageSpecFactory()).thenReturn(new DeviceMessageSpecFactoryImpl());
        ManagerFactory.setCurrent(serverManager);
    }

    @Test
    public void testBillingResetMessage(){
        Messaging meterProtocol = new ABBA1140();
        final ABBA1140MessageConverter messageConverter = new ABBA1140MessageConverter();
        messageConverter.setMessagingProtocol(meterProtocol);
        OfflineDeviceMessage contactorOpen = mock(OfflineDeviceMessage.class);
        when(contactorOpen.getDeviceMessageSpecPrimaryKey()).thenReturn(DeviceActionMessage.BILLING_RESET.getPrimaryKey());

        // business method
        final MessageEntry messageEntry = messageConverter.toMessageEntry(contactorOpen);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<BillingReset></BillingReset>");
    }

}
