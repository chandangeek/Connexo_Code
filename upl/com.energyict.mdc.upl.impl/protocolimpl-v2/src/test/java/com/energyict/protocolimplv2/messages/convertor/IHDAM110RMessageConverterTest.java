package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.LegacyMessageConverter;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.ihd.InHomeDisplay;
import org.junit.Test;

import java.text.ParseException;

import static junit.framework.Assert.assertEquals;

/**
 * Created by cisac on 8/17/2015.
 */
public class IHDAM110RMessageConverterTest extends AbstractV2MessageConverterTest {

    @Test
    public void testMessageConversion() {
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;

        offlineDeviceMessage = createMessage(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<FirmwareUpgrade><IncludedFile>path</IncludedFile></FirmwareUpgrade>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<FirmwareUpgrade><IncludedFile>path</IncludedFile><ActivationDate>28/10/2013 10:00:00</ActivationDate></FirmwareUpgrade>", messageEntry.getContent());
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new InHomeDisplay(propertySpecService, deviceMessageFileFinder, deviceMessageFileExtractor);
    }

    @Override
    LegacyMessageConverter doGetMessageConverter() {
        return new IHDAM110RMessageConverter(getMessagingProtocol(), propertySpecService, nlsService, converter);
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
