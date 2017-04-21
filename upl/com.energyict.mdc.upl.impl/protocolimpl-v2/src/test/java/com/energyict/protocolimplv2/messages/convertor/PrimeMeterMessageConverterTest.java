package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.LegacyMessageConverter;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.HexString;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.protocolimpl.dlms.prime.PrimeMeter;
import com.energyict.protocolimplv2.eict.eiweb.SimplePassword;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadBalanceDeviceMessage;
import com.energyict.protocolimplv2.messages.PLCConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.MulticastAddress1AttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.MulticastAddress2AttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.MulticastAddress3AttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activationDatedAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contractsXmlUserFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newFirmwareClientPasswordAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newManagementClientPasswordAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newReadingClientPasswordAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.relayNumberAttributeName;
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
public class PrimeMeterMessageConverterTest extends AbstractV2MessageConverterTest {

    @Test
    public void testMessageConversion() throws IOException {
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;

        offlineDeviceMessage = createMessage(ContactorDeviceMessage.CLOSE_RELAY);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<ConnectRelay2> \n\n</ConnectRelay2>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<FirmwareUpdate><IncludedFile>path</IncludedFile></FirmwareUpdate>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ActivityCalendarDeviceMessage.WRITE_CONTRACTS_FROM_XML_USERFILE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<WriteContracts>XML content\n\n</WriteContracts>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(LoadBalanceDeviceMessage.WriteControlThresholds);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<WriteControlThresholds Threshold 1 (unit W)=\"1\" Threshold 2 (unit W)=\"1\" Threshold 3 (unit W)=\"1\" Threshold 4 (unit W)=\"1\" Threshold 5 (unit W)=\"1\" Threshold 6 (unit W)=\"1\" ActivationDate=\"10000000000000\"> \n\n</WriteControlThresholds>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(PLCConfigurationDeviceMessage.SetMulticastAddresses);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<SetMulticastAddresses Address 1=\"FFFFFFFF\" Address 2=\"FFFFFFFF\" Address 3=\"FFFFFFFF\"> \n\n</SetMulticastAddresses>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(SecurityMessage.CHANGE_CLIENT_PASSWORDS);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<ChangePasswords reading=\"abcdefgh\" management=\"abcdefgh\" firmware=\"abcdefgh\"> \n\n</ChangePasswords>", messageEntry.getContent());
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new PrimeMeter(propertySpecService, nlsService);
    }

    protected LegacyMessageConverter doGetMessageConverter() {
        return new PrimeMeterMessageConverter(propertySpecService, nlsService, converter, deviceMessageFileExtractor);
    }

    /**
     * Gets the value to use for the given {@link com.energyict.mdc.upl.properties.PropertySpec}
     */
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        if (propertySpec.getName().equals(relayNumberAttributeName)) {
            return BigDecimal.valueOf(2);
        } else if (propertySpec.getName().equals(contractsXmlUserFileAttributeName)) {
            DeviceMessageFile deviceMessageFile = mock(DeviceMessageFile.class);
            when(deviceMessageFileExtractor.contents(deviceMessageFile)).thenReturn("XML content");
            return deviceMessageFile;
        } else if (propertySpec.getName().equals(firmwareUpdateFileAttributeName)) {
            return "path";
        } else if (propertySpec.getName().equals(newManagementClientPasswordAttributeName)
                || propertySpec.getName().equals(newFirmwareClientPasswordAttributeName)
                || propertySpec.getName().equals(newReadingClientPasswordAttributeName)) {
            return new SimplePassword("abcdefgh");
        } else if (propertySpec.getName().equals(MulticastAddress1AttributeName)
                || propertySpec.getName().equals(MulticastAddress2AttributeName)
                || propertySpec.getName().equals(MulticastAddress3AttributeName)) {
            return (HexString) () -> "FFFFFFFF";
        } else if (propertySpec.getName().equals(activationDatedAttributeName)) {
            return new Date(10000000000000L);
        }
        return "1";     //All other attribute values are "1"
    }
}