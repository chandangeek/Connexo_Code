package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.LegacyMessageConverter;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.properties.Password;
import com.energyict.mdw.core.UserFile;
import com.energyict.protocolimpl.dlms.eictz3.EictZ3;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.text.ParseException;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorModeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateUserFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.openKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.transferKeyAttributeName;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

        offlineDeviceMessage = createMessage(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<FirmwareUpdate><IncludedFile>Firmware bytes</IncludedFile></FirmwareUpdate>", messageEntry.getContent());

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
            try {
                return dateTimeFormat.parse("1970/01/01 01:00:00");
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (propertySpec.getName().equals(contactorModeAttributeName)) {
            return BigDecimal.valueOf(1);
        } else if (propertySpec.getName().equals(openKeyAttributeName) || propertySpec.getName().equals(transferKeyAttributeName)) {
            return new Password("0101001010101010");
        } else if (propertySpec.getName().equals(firmwareUpdateUserFileAttributeName)) {
            UserFile userFile = mock(UserFile.class);
            when(userFile.loadFileInByteArray()).thenReturn("Firmware bytes".getBytes());
            return userFile;
        }
        return "1";     //All other attribute values are "1"
    }
}