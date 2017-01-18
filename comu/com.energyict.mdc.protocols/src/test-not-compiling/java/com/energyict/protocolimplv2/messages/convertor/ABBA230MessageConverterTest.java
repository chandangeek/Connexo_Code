package com.energyict.protocolimplv2.messages.convertor;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocols.messaging.LegacyMessageConverter;
import com.energyict.mdc.protocol.api.UserFile;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.messaging.Messaging;
import com.energyict.protocolimpl.iec1107.abba230.ABBA230;


import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.nio.charset.Charset;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test that creates OfflineDeviceMessages (the attributes are all filled with dummy values) and converts them to the legacy XML message,
 * testing the {@link ABBA230MessageConverter} component.
 *
 * @author sva
 * @since 24/10/13 - 15:38
 */
@RunWith(MockitoJUnitRunner.class)
public class ABBA230MessageConverterTest extends AbstractMessageConverterTest {

    @Test
    public void testMessageConversion() {
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;

        offlineDeviceMessage = createMessage(DeviceMessageId.CONTACTOR_OPEN);
        messageEntry =  getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<ConnectLoad></ConnectLoad>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceMessageId.CONTACTOR_CLOSE);
        messageEntry =  getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<DisconnectLoad></DisconnectLoad>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceMessageId.CONTACTOR_ARM);
        messageEntry =  getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<ArmMeter></ArmMeter>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceActionMessage.DEMAND_RESET);
        messageEntry =  getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<BillingReset></BillingReset>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.UploadMeterScheme);
        messageEntry =  getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<UpgradeMeterScheme><XML>content_MeterScheme</XML></UpgradeMeterScheme>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE);
        messageEntry =  getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<UpgradeMeterFirmware><XML>content_FirmwareUpdate</XML></UpgradeMeterFirmware>", messageEntry.getContent());
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new ABBA230();
    }

    @Override
    LegacyMessageConverter doGetMessageConverter() {
        return new ABBA230MessageConverter();
    }

    @Override
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.MeterScheme:
                UserFile mockedUserFile = mock(UserFile.class);
                when(mockedUserFile.loadFileInByteArray()).thenReturn("<XML>content_MeterScheme</XML>".getBytes(Charset.forName("UTF-8")));
                return mockedUserFile;
            case DeviceMessageConstants.firmwareUpdateUserFileAttributeName:
                mockedUserFile = mock(UserFile.class);
                when(mockedUserFile.loadFileInByteArray()).thenReturn("<XML>content_FirmwareUpdate</XML>".getBytes(Charset.forName("UTF-8")));
                return mockedUserFile;
            default:
                return "0";
        }
    }
}
