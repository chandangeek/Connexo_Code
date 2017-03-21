package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimpl.iec1107.abba1140.ABBA1140;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
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
 * <p>
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 16:46
 */
@RunWith(MockitoJUnitRunner.class)
public class ABBA1140MessageConverterTest {

    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private NlsService nlsService;
    @Mock
    private Converter converter;

    @Test
    public void testBillingResetMessage() {
        Messaging meterProtocol = new ABBA1140(propertySpecService);
        final ABBA1140MessageConverter messageConverter = new ABBA1140MessageConverter(meterProtocol, propertySpecService, nlsService, converter);
        OfflineDeviceMessage contactorOpen = mock(OfflineDeviceMessage.class);
        when(contactorOpen.getDeviceMessageId()).thenReturn(DeviceActionMessage.BILLING_RESET.id());

        // business method
        final MessageEntry messageEntry = messageConverter.toMessageEntry(contactorOpen);

        // asserts
        assertNotNull(messageEntry);
        assertThat(messageEntry.getContent()).isEqualTo("<BillingReset></BillingReset>");
    }

}
