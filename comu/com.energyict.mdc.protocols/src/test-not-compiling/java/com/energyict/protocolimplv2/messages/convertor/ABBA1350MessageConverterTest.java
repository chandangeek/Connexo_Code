package com.energyict.protocolimplv2.messages.convertor;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocols.messaging.LegacyMessageConverter;
import com.energyict.mdc.protocol.api.UserFile;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.messaging.Messaging;
import com.energyict.protocolimpl.iec1107.abba1350.ABBA1350;

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
 * testing the {@link ABBA1350MessageConverter} component.
 *
 * @author sva
 * @since 25/10/13 - 9:40
 */
@RunWith(MockitoJUnitRunner.class)
public class ABBA1350MessageConverterTest extends AbstractMessageConverterTest {

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
        return new ABBA1350();
    }

    @Override
    LegacyMessageConverter doGetMessageConverter() {
        return new ABBA1350MessageConverter();
    }

    @Override
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.SwitchPointClockSettings:
                UserFile mockedUserFile = mock(UserFile.class);
                when(mockedUserFile.loadFileInByteArray()).thenReturn("content_SPC".getBytes(Charset.forName("UTF-8")));
                return mockedUserFile;
            case DeviceMessageConstants.SwitchPointClockUpdateSettings:
                mockedUserFile = mock(UserFile.class);
                when(mockedUserFile.loadFileInByteArray()).thenReturn("content_SPCU".getBytes(Charset.forName("UTF-8")));
                return mockedUserFile;
            default:
                return "0";
        }
    }
}
