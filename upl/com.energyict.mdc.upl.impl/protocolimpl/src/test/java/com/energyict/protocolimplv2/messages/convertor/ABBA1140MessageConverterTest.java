package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.Manager;
import com.energyict.mdc.messages.DeviceMessageSpecFactory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.Messaging;

import com.energyict.mdw.interfacing.mdc.MdcInterface;
import com.energyict.mdw.interfacing.mdc.MdcInterfaceProvider;
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
    private Manager manager;
    @Mock
    private DeviceMessageSpecFactory deviceMessageSpecFactory;
    @Mock
    private MdcInterface mdcInterface;
    @Mock
    private MdcInterfaceProvider mdcInterfaceProvider;

    @Before
    public void beforeEachTest() {
        MdcInterfaceProvider.instance.set(mdcInterfaceProvider);
        when(mdcInterfaceProvider.getMdcInterface()).thenReturn(mdcInterface);
        when(mdcInterface.getManager()).thenReturn(manager);
        when(manager.getDeviceMessageSpecFactory()).thenReturn(deviceMessageSpecFactory);
        final ABBA1140MessageConverter abba1140MessageConverter = new ABBA1140MessageConverter();
        for (DeviceMessageSpec deviceMessageSpec : abba1140MessageConverter.getSupportedMessages()) {
            when(deviceMessageSpecFactory.fromPrimaryKey(deviceMessageSpec.getPrimaryKey().getValue())).thenReturn(deviceMessageSpec);
        }
    }

    @Test
    public void testBillingResetMessage(){
        Messaging meterProtocol = new ABBA1140();
        final ABBA1140MessageConverter messageConverter = new ABBA1140MessageConverter();
        messageConverter.setMessagingProtocol(meterProtocol);
        OfflineDeviceMessage contactorOpen = mock(OfflineDeviceMessage.class);
        when(contactorOpen.getDeviceMessageSpecPrimaryKey()).thenReturn(DeviceActionMessage.BILLING_RESET.getPrimaryKey().getValue());

        // business method
        final MessageEntry messageEntry = messageConverter.toMessageEntry(contactorOpen);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<BillingReset></BillingReset>");
    }

}
