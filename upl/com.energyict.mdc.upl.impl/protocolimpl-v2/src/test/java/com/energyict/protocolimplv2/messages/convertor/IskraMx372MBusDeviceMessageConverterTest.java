package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.LegacyMessageConverter;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.MBusConfigurationDeviceMessage;
import com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.MbusDevice;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertEquals;

/**
 * Test that creates OfflineDeviceMessages (the attributes are all filled with dummy values) and converts them to the legacy XML message,
 * testing the {@link IskraMx372MBusDeviceMessageConverter} component.
 *
 * @author sva
 * @since 29/10/13 - 10:19
 */
@RunWith(MockitoJUnitRunner.class)
public class IskraMx372MBusDeviceMessageConverterTest extends AbstractMessageConverterTest {

    @Test
    public void testMessageConversion() {
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;

        offlineDeviceMessage = createMessage(ContactorDeviceMessage.CONTACTOR_CLOSE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<connectLoad> </connectLoad>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ContactorDeviceMessage.CONTACTOR_OPEN);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<disconnectLoad> </disconnectLoad>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(MBusConfigurationDeviceMessage.SetMBusVIF);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Mbus_Set_VIF>0123456789ABCDEF</Mbus_Set_VIF>", messageEntry.getContent());
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new MbusDevice();
    }

    @Override
    LegacyMessageConverter doGetMessageConverter() {
        return new IskraMx372MBusDeviceMessageConverter();
    }

    @Override
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.SetMBusVIFAttributeName:
                return "0123456789ABCDEF";
            default:
                return "";
        }
    }
}