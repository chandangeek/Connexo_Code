package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cbo.HexString;
import com.energyict.cbo.Password;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.LegacyMessageConverter;
import com.energyict.mdw.core.UserFile;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimpl.dlms.prime.PrimeMeter;
import com.energyict.protocolimplv2.messages.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;
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
public class PrimeMeterMessageConverterTest extends AbstractMessageConverterTest {

    @Test
    public void testMessageConversion() throws IOException {
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;

        offlineDeviceMessage = createMessage(ContactorDeviceMessage.CLOSE_RELAY);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<ConnectRelay2> \n\n</ConnectRelay2>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<FirmwareUpdate><IncludedFile>Firmware bytes</IncludedFile></FirmwareUpdate>", messageEntry.getContent());

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
        return new PrimeMeter();
    }

    protected LegacyMessageConverter doGetMessageConverter() {
        return new PrimeMeterMessageConverter();
    }

    /**
     * Gets the value to use for the given {@link com.energyict.cpo.PropertySpec}
     */
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        if (propertySpec.getName().equals(relayNumberAttributeName)) {
            return BigDecimal.valueOf(2);
        } else if (propertySpec.getName().equals(contractsXmlUserFileAttributeName)) {
            UserFile userFile = mock(UserFile.class);
            when(userFile.loadFileInByteArray()).thenReturn("XML content".getBytes());
            return userFile;
        } else if (propertySpec.getName().equals(firmwareUpdateUserFileAttributeName)) {
            UserFile userFile = mock(UserFile.class);
            when(userFile.loadFileInByteArray()).thenReturn("Firmware bytes".getBytes());
            return userFile;
        } else if (propertySpec.getName().equals(newManagementClientPasswordAttributeName)
                || propertySpec.getName().equals(newFirmwareClientPasswordAttributeName)
                || propertySpec.getName().equals(newReadingClientPasswordAttributeName)) {
            return new Password("abcdefgh");
        } else if (propertySpec.getName().equals(MulticastAddress1AttributeName)
                || propertySpec.getName().equals(MulticastAddress2AttributeName)
                || propertySpec.getName().equals(MulticastAddress3AttributeName)) {
            return new HexString("FFFFFFFF");
        } else if (propertySpec.getName().equals(activationDatedAttributeName)) {
            return new Date(10000000000000L);
        }
        return "1";     //All other attribute values are "1"
    }
}