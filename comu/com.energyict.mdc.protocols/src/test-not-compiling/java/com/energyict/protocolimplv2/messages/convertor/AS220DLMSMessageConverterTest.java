package com.energyict.protocolimplv2.messages.convertor;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocols.messaging.LegacyMessageConverter;
import com.energyict.mdc.protocol.api.UserFile;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;


import com.energyict.mdc.protocol.api.messaging.Messaging;
import com.energyict.protocolimpl.dlms.as220.AS220;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.activityCalendarActivationDateAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.firmwareUpdateUserFileAttributeName;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test that creates OfflineDeviceMessages (the attributes are all filled with dummy values) and converts them to the legacy XML message,
 * testing the {@link com.energyict.protocolimplv2.messages.convertor.ModbusMessageConverter} component.
 *
 * @author khe
 * @since 24/10/13 - 10:50
 */
@RunWith(MockitoJUnitRunner.class)
public class AS220DLMSMessageConverterTest extends AbstractMessageConverterTest {

    @Test
    public void testMessageConversion() {
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;

        offlineDeviceMessage = createMessage(ActivityCalendarDeviceMessage.ACTIVATE_PASSIVE_CALENDAR);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<ActivatePassiveCalendar ActivationTime=\"1970/01/01 01:00:00\"> </ActivatePassiveCalendar>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(PLCConfigurationDeviceMessage.SetPlcChannelFreqSnrCredits);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<SetPlcChannelFreqSnrCredits CHANNEL1_FS=\"1\" CHANNEL1_FM=\"1\" CHANNEL1_SNR=\"1\" CHANNEL1_CREDITWEIGHT=\"1\" CHANNEL2_FS=\"1\" CHANNEL2_FM=\"1\" CHANNEL2_SNR=\"1\" CHANNEL2_CREDITWEIGHT=\"1\" CHANNEL3_FS=\"1\" CHANNEL3_FM=\"1\" CHANNEL3_SNR=\"1\" CHANNEL3_CREDITWEIGHT=\"1\" CHANNEL4_FS=\"1\" CHANNEL4_FM=\"1\" CHANNEL4_SNR=\"1\" CHANNEL4_CREDITWEIGHT=\"1\" CHANNEL5_FS=\"1\" CHANNEL5_FM=\"1\" CHANNEL5_SNR=\"1\" CHANNEL5_CREDITWEIGHT=\"1\" CHANNEL6_FS=\"1\" CHANNEL6_FM=\"1\" CHANNEL6_SNR=\"1\" CHANNEL6_CREDITWEIGHT=\"1\"> </SetPlcChannelFreqSnrCredits>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceMessageId.MBUS_SETUP_DECOMMISSIONAll);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<DecommissionAll> </DecommissionAll>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<FirmwareUpdate><IncludedFile>userFileBytes</IncludedFile></FirmwareUpdate>", messageEntry.getContent());
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new AS220();
    }

    protected LegacyMessageConverter doGetMessageConverter() {
        return new AS220DLMSMessageConverter();
    }

    /**
     * Gets the value to use for the given {@link com.energyict.cpo.PropertySpec}
     */
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        if (propertySpec.getName().equals(activityCalendarActivationDateAttributeName)) {
            return new Date(0);
        } else if (propertySpec.getName().equals(firmwareUpdateUserFileAttributeName)) {
            UserFile userFile = mock(UserFile.class);
            when(userFile.getId()).thenReturn(1121);
            when(userFile.loadFileInByteArray()).thenReturn("userFileBytes".getBytes());
            return userFile;
        }
        return "1";     //All other attribute values are "1"
    }
}