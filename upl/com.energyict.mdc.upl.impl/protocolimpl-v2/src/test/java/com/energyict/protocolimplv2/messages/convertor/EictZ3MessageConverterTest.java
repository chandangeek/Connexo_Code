package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cbo.HexString;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.LegacyMessageConverter;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimpl.dlms.eictz3.EictZ3;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Date;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;
import static org.junit.Assert.assertEquals;

/**
 * Test that creates OfflineDeviceMessages (the attributes are all filled with dummy values) and converts them to the legacy XML message,
 * testing the {@link ModbusMessageConverter} component.
 *
 * @author khe
 * @since 24/10/13 - 10:50
 */
@RunWith(MockitoJUnitRunner.class)
public class EictZ3MessageConverterTest extends AbstractMessageConverterTest {

    @Test
    public void testMessageConversion() {
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;

        offlineDeviceMessage = createMessage(MBusSetupDeviceMessage.SetEncryptionKeys);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Set_Encryption_keys Open_Key_Value=\"0101001010101010\" Transfer_Key_Value=\"0101001010101010\"> </Set_Encryption_keys>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<disconnectLoad Activation_date=\"1970/01/01 01:00:00\"> </disconnectLoad>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(MBusSetupDeviceMessage.Decommission);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Decommission/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Connect_control_mode Mode=\"1\"> </Connect_control_mode>", messageEntry.getContent());

    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new EictZ3();
    }

    protected LegacyMessageConverter doGetMessageConverter() {
        return new EictZ3MessageConverter();
    }

    /**
     * Gets the value to use for the given {@link com.energyict.cpo.PropertySpec}
     */
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        if (propertySpec.getName().equals(contactorActivationDateAttributeName)) {
            return new Date(0);
        } else if (propertySpec.getName().equals(contactorModeAttributeName)) {
            return BigDecimal.valueOf(1);
        } else if (propertySpec.getName().equals(openKeyAttributeName) || propertySpec.getName().equals(transferKeyAttributeName)) {
            return new HexString("0101001010101010");
        }
        return "1";     //All other attribute values are "1"
    }
}