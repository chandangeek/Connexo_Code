package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.LegacyMessageConverter;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.protocolimpl.iec1107.abba1350.ABBA1350;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
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
 * testing the {@link ABBA1350MessageConverter} component.
 *
 * @author sva
 * @since 25/10/13 - 9:40
 */
@RunWith(MockitoJUnitRunner.class)
public class ABBA1350MessageConverterTest extends AbstractV2MessageConverterTest {

    @Test
    public void testMessageConversion() {
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;

        offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.UploadSwitchPointClockSettings);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<SPC_DATA>content_SPC</SPC_DATA>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ConfigurationChangeDeviceMessage.UploadSwitchPointClockUpdateSettings);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<SPCU_DATA>content_SPCU</SPCU_DATA>", messageEntry.getContent());
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new ABBA1350(propertySpecService, nlsService);
    }

    @Override
    LegacyMessageConverter doGetMessageConverter() {
        return new ABBA1350MessageConverter(propertySpecService, nlsService, converter, deviceMessageFileExtractor);
    }

    @Override
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.SwitchPointClockSettings:
                DeviceMessageFile deviceMessageFile1 = mock(DeviceMessageFile.class);
                when(deviceMessageFileExtractor.contents(eq(deviceMessageFile1), any(Charset.class))).thenReturn("content_SPC");
                return deviceMessageFile1;
            case DeviceMessageConstants.SwitchPointClockUpdateSettings:
                DeviceMessageFile deviceMessageFile2 = mock(DeviceMessageFile.class);
                when(deviceMessageFileExtractor.contents(eq(deviceMessageFile2), any(Charset.class))).thenReturn("content_SPCU");
                return deviceMessageFile2;
            default:
                return "0";
        }
    }
}