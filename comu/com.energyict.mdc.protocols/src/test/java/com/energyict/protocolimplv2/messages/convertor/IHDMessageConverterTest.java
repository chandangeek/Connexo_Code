package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.messages.LegacyMessageConverter;
import com.energyict.mdw.core.UserFile;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.ihd.InHomeDisplay;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.text.ParseException;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test that creates OfflineDeviceMessages (the attributes are all filled with dummy values) and converts them to the legacy XML message,
 * testing the {@link IHDMessageConverter} component.
 *
 * @author sva
 * @since 28/10/13 - 10:26
 */
@RunWith(MockitoJUnitRunner.class)
public class IHDMessageConverterTest extends AbstractMessageConverterTest {

    @Test
    public void testMessageConversion() {
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;

        offlineDeviceMessage = createMessage(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<FirmwareUpgrade UserFileID=\"1\"> </FirmwareUpgrade>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<FirmwareUpgrade UserFileID=\"1\" Activation_date=\"28/10/2013 10:00:00\"> </FirmwareUpgrade>", messageEntry.getContent());
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new InHomeDisplay();
    }

    @Override
    LegacyMessageConverter doGetMessageConverter() {
        return new IHDMessageConverter();
    }

    @Override
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        try {
            switch (propertySpec.getName()) {
                case DeviceMessageConstants.firmwareUpdateUserFileAttributeName:
                    UserFile mockedUserFile = mock(UserFile.class);
                    when(mockedUserFile.getId()).thenReturn(1);
                    return mockedUserFile;
                case DeviceMessageConstants.firmwareUpdateActivationDateAttributeName:
                    return europeanDateTimeFormat.parse("28/10/2013 10:00:00");
                default:
                    return "";
            }
        } catch (ParseException e) {
            return "";
        }
    }
}