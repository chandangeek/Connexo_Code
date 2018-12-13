package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.LegacyMessageConverter;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.ihd.InHomeDisplay;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.text.ParseException;

import static junit.framework.Assert.assertEquals;

/**
 * Test that creates OfflineDeviceMessages (the attributes are all filled with dummy values) and converts them to the legacy XML message,
 * testing the {@link IHDMessageConverter} component.
 *
 * @author sva
 * @since 28/10/13 - 10:26
 */
@RunWith(MockitoJUnitRunner.class)
public class IHDMessageConverterTest extends AbstractV2MessageConverterTest {

    @Test
    public void testMessageConversion() {
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;

        offlineDeviceMessage = createMessage(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<FirmwareUpgrade FirmwareFilePath=\"path\"> </FirmwareUpgrade>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<FirmwareUpgrade FirmwareFilePath=\"path\" Activation_date=\"28/10/2013 10:00:00\"> </FirmwareUpgrade>", messageEntry.getContent());
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new InHomeDisplay(propertySpecService, this.deviceMessageFileFinder, deviceMessageFileExtractor);
    }

    @Override
    LegacyMessageConverter doGetMessageConverter() {
        return new IHDMessageConverter(propertySpecService, this.nlsService, this.converter);
    }

    @Override
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        try {
            switch (propertySpec.getName()) {
                case DeviceMessageConstants.firmwareUpdateFileAttributeName:
                    return "path";
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