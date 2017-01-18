package com.energyict.protocolimplv2.messages.convertor;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocols.messaging.LegacyMessageConverter;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.messaging.Messaging;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;

import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict.MbusDevice;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.text.ParseException;

import static junit.framework.Assert.assertEquals;

/**
 * Test that creates OfflineDeviceMessages (the attributes are all filled with dummy values) and converts them to the legacy XML message,
 * testing the {@link Dsmr23MBusDeviceMessageConverter} component.
 *
 * @author sva
 * @since 30/10/13 - 9:06
 */
@RunWith(MockitoJUnitRunner.class)
public class Dsmr23MBusDeviceMessageConverterTest extends AbstractMessageConverterTest {

    @Test
    public void testMessageConversion() {
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;

        offlineDeviceMessage = createMessage(DeviceMessageId.CONTACTOR_CLOSE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<connectLoad> </connectLoad>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceMessageId.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<connectLoad Activation_date=\"1383123600\"> </connectLoad>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceMessageId.CONTACTOR_OPEN);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<disconnectLoad> </disconnectLoad>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<disconnectLoad Activation_date=\"1383123600\"> </disconnectLoad>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceMessageId.CONTACTOR_CHANGE_CONNECT_CONTROL_MODE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Connect_control_mode Mode=\"1\"> </Connect_control_mode>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceMessageId.MBUS_SETUP_DECOMMISSION);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Decommission/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceMessageId.MBUS_SETUP_SET_ENCRYPTION_KEYS);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Set_Encryption_keys Open_Key_Value=\"open\" Transfer_Key_Value=\"transfer\"> </Set_Encryption_keys>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceMessageId.MBUS_SETUP_USE_CORRECTED_VALUES);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Corrected_values/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceMessageId.MBUS_SETUP_USE_UNCORRECTED_VALUES);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<UnCorrected_values/>", messageEntry.getContent());
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new MbusDevice();
    }

    @Override
    LegacyMessageConverter doGetMessageConverter() {
        return new Dsmr23MBusDeviceMessageConverter();
    }

    @Override
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        try {
            switch (propertySpec.getName()) {
                case DeviceMessageConstants.contactorActivationDateAttributeName:
                    return europeanDateTimeFormat.parse("30/10/2013 10:00:00");
                case DeviceMessageConstants.contactorModeAttributeName:
                    return 1;
                case DeviceMessageConstants.openKeyAttributeName:
                    return "open";
                case DeviceMessageConstants.transferKeyAttributeName:
                    return "transfer";
                default:
                    return "";
            }
        } catch (ParseException e) {
            return "";
        }
    }
}
