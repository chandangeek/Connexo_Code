package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.LegacyMessageConverter;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.protocolimpl.iec1107.abba230.ABBA230;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.nio.charset.Charset;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
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
public class ABBA230MessageConverterTest extends AbstractV2MessageConverterTest {

    @Test
    public void testMessageConversion() {
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;

        offlineDeviceMessage = createMessage(ContactorDeviceMessage.CONTACTOR_OPEN);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<ConnectLoad></ConnectLoad>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ContactorDeviceMessage.CONTACTOR_CLOSE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<DisconnectLoad></DisconnectLoad>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ContactorDeviceMessage.CONTACTOR_ARM);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<ArmMeter></ArmMeter>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceActionMessage.DEMAND_RESET);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<DemandReset></DemandReset>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.UploadMeterScheme);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<UploadMeterScheme><XML>content_MeterScheme</XML></UploadMeterScheme>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<UpgradeMeterFirmware>path</UpgradeMeterFirmware>", messageEntry.getContent());
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new ABBA230(propertySpecService);
    }

    @Override
    LegacyMessageConverter doGetMessageConverter() {
        return new ABBA230MessageConverter(propertySpecService, nlsService, converter, deviceMessageFileExtractor);
    }

    @Override
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.MeterScheme:
                DeviceMessageFile deviceMessageFile1 = mock(DeviceMessageFile.class);
                when(deviceMessageFileExtractor.contents(eq(deviceMessageFile1), any(Charset.class))).thenReturn("<XML>content_MeterScheme</XML>");
                return deviceMessageFile1;
            case DeviceMessageConstants.firmwareUpdateFileAttributeName:
                return "path";
            default:
                return "0";
        }
    }
}
