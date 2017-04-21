package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.protocolimpl.iec1107.abba1140.ABBA1140;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ABBA1140MessageConverter} component
 * <p>
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 16:46
 */
@RunWith(MockitoJUnitRunner.class)
public class ABBA1140MessageConverterTest extends AbstractMessageConverterTest {

    @Test
    public void testBillingResetMessage() {
        Messaging meterProtocol = new ABBA1140(propertySpecService);
        final ABBA1140MessageConverter messageConverter = new ABBA1140MessageConverter(propertySpecService, nlsService, converter);
        OfflineDeviceMessage contactorOpen = mock(OfflineDeviceMessage.class);
        when(contactorOpen.getDeviceMessageId()).thenReturn(DeviceActionMessage.BILLING_RESET.id());
        when(contactorOpen.getSpecification()).thenReturn(DeviceActionMessage.BILLING_RESET.get(propertySpecService, nlsService, converter));

        // business method
        final MessageEntry messageEntry = messageConverter.toMessageEntry(contactorOpen);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<BillingReset></BillingReset>");
    }

}