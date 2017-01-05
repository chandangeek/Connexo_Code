package com.energyict.protocolimplv2.messages.convertor;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocols.messaging.LegacyMessageConverter;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.messaging.Messaging;
import com.energyict.protocolimpl.elster.a1800.A1800;

import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertEquals;

/**
 * Test that creates OfflineDeviceMessages (the attributes are all filled with dummy values) and converts them to the legacy XML message,
 * testing the {@link ElsterA1800MessageConverter} component.
 *
 * @author sva
 * @since 25/10/13 - 10:49
 */
@RunWith(MockitoJUnitRunner.class)
public class ElsterA1800MessageConverterTest extends AbstractMessageConverterTest {

    @Test
    public void testMessageConversion() {
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;

        offlineDeviceMessage = createMessage(ChannelConfigurationDeviceMessage.SetLPDivisor);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<SETLPDIVISOR Channel=\"1\" Divisor=\"2\"> </SETLPDIVISOR>", messageEntry.getContent());
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new A1800();
    }

    @Override
    LegacyMessageConverter doGetMessageConverter() {
        return new ElsterA1800MessageConverter();
    }

    @Override
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.ChannelConfigurationChnNbrAttributeName:
                return "1";
            case DeviceMessageConstants.DivisorAttributeName:
                return "2";
            default:
                return "0";
        }
    }
}
