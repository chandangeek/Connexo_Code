package com.energyict.protocolimplv2.messages.convertor;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocols.messaging.LegacyMessageConverter;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.messaging.Messaging;
import com.energyict.protocolimpl.dlms.iskrame37x.IskraME37X;
import com.energyict.mdc.protocol.api.impl.device.messages.ContactorDeviceMessage;
import com.energyict.mdc.protocol.api.impl.device.messages.DeviceActionMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Test that creates OfflineDeviceMessages (the attributes are all filled with dummy values) and converts them to the legacy XML message,
 * testing the {@link com.energyict.protocolimplv2.messages.convertor.ModbusMessageConverter} component.
 *
 * @author khe
 * @since 24/10/13 - 10:50
 */
@RunWith(MockitoJUnitRunner.class)
public class IskraME37XMessageConverterTest extends AbstractMessageConverterTest {

    @Test
    public void testMessageConversion() throws IOException {
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;

        offlineDeviceMessage = createMessage(DeviceActionMessage.DEMAND_RESET);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<DemandReset></DemandReset>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ContactorDeviceMessage.CONTACTOR_OPEN);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<DISCONNECT> </DISCONNECT>", messageEntry.getContent());
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new IskraME37X();
    }

    protected LegacyMessageConverter doGetMessageConverter() {
        return new IskraME37XMessageConverter();
    }

    /**
     * Gets the value to use for the given {@link com.energyict.cpo.PropertySpec}
     */
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        return "";
    }
}