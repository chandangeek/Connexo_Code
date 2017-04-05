package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.LegacyMessageConverter;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.smartmeterprotocolimpl.iskra.mt880.IskraMT880;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertEquals;

/**
 * Test that creates OfflineDeviceMessages (the attributes are all filled with dummy values) and converts them to the legacy XML message,
 * testing the {@link com.energyict.protocolimplv2.messages.convertor.NXT4MessageConverter} component.
 *
 * @author sva
 * @since 19/11/14 - 13:31
 */
@RunWith(MockitoJUnitRunner.class)
public class NXT4MessageConverterTest extends AbstractV2MessageConverterTest {

    @Test
    public void testMessageConversion() {
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;

        offlineDeviceMessage = createMessage(DeviceActionMessage.DEMAND_RESET);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<DemandReset/>", messageEntry.getContent());
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new IskraMT880(propertySpecService);
    }

    @Override
    LegacyMessageConverter doGetMessageConverter() {
        return new IskraMT880MessageConverter(getMessagingProtocol(), propertySpecService, nlsService, converter);
    }

    @Override
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        return "";
    }
}