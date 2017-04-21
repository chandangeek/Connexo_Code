package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.LegacyMessageConverter;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.protocolimpl.iec1107.abba1700.ABBA1700;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertEquals;

/**
 * Test that creates OfflineDeviceMessages (the attributes are all filled with dummy values) and converts them to the legacy XML message,
 * testing the {@link ABBA1700MessageConverter} component.
 *
 * @author sva
 * @since 25/10/13 - 10:12
 */
@RunWith(MockitoJUnitRunner.class)
public class ABBA1700MessageConverterTest extends AbstractV2MessageConverterTest {

    @Test
    public void testMessageConversion() {
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;

        offlineDeviceMessage = createMessage(DeviceActionMessage.BILLING_RESET);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<DemandReset/>", messageEntry.getContent());
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new ABBA1700(propertySpecService);
    }

    @Override
    LegacyMessageConverter doGetMessageConverter() {
        return new ABBA1700MessageConverter(propertySpecService, nlsService, converter);
    }

    @Override
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        return "0";
    }
}
