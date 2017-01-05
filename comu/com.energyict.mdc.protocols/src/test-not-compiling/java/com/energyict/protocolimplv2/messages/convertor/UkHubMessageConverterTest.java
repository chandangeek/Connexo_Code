package com.energyict.protocolimplv2.messages.convertor;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocols.messaging.LegacyMessageConverter;
import com.energyict.mdc.protocol.api.UserFile;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.messaging.Messaging;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;

import com.energyict.smartmeterprotocolimpl.eict.ukhub.UkHub;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.text.ParseException;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test that creates OfflineDeviceMessages (the attributes are all filled with dummy values) and converts them to the legacy XML message,
 * testing the {@link UkHubMessageConverter} component.
 *
 * @author sva
 * @since 25/10/13 - 15:17
 */
@RunWith(MockitoJUnitRunner.class)
public class UkHubMessageConverterTest extends AbstractMessageConverterTest {

    @Test
    public void testMessageConversion() {
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;

        offlineDeviceMessage = createMessage(DeviceMessageId.DEVICE_ACTIONS_DISABLE_WEBSERVER);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Disable_Webserver/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceMessageId.DEVICE_ACTIONS_ENABLE_WEBSERVER);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Enable_Webserver/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceMessageId.NETWORK_CONNECTIVITY_CONFIGURE_KEEP_ALIVE_SETTINGS);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<GPRS_Modem_Ping_Setup Ping_IP=\"127.0.0.1:80\" Ping_Interval=\"120\"> </GPRS_Modem_Ping_Setup>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceMessageId.LOG_BOOK_READ_DEBUG);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Debug_Logbook From_date=\"01/10/2013 00:00:00\" To_date=\"15/10/2013 00:00:00\"> </Debug_Logbook>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceMessageId.LOG_BOOK_READ_MANUFACTURER_SPECIFIC);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Elster_Specific_Logbook From_date=\"01/10/2013 00:00:00\" To_date=\"15/10/2013 00:00:00\"> </Elster_Specific_Logbook>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceMessageId.ZIGBEE_CONFIGURATION_CREATE_HAN_NETWORK);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Create_Han_Network/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceMessageId.ZIGBEE_CONFIGURATION_REMOVE_HAN_NETWORK);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Remove_Han_Network/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceMessageId.ZIGBEE_CONFIGURATION_JOIN_SLAVE_DEVICE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Join_ZigBee_Slave ZigBee_IEEE_Address=\"ABC\" ZigBee_Link_Key=\"123\"> </Join_ZigBee_Slave>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceMessageId.ZIGBEE_CONFIGURATION_REMOVE_MIRROR);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Remove_Mirror Mirror_IEEE_Address=\"1\" Force_Removal=\"false\"> </Remove_Mirror>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceMessageId.ZIGBEE_CONFIGURATION_REMOVE_SLAVE_DEVICE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Remove_ZigBee_Slave ZigBee_IEEE_Address=\"ABC\"> </Remove_ZigBee_Slave>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceMessageId.ZIGBEE_CONFIGURATION_REMOVE_ALL_SLAVE_DEVICES);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Remove_All_ZigBee_Slaves/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceMessageId.ZIGBEE_CONFIGURATION_BACK_UP_HAN_PARAMETERS);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Backup_ZigBee_Han_Parameters/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceMessageId.ZIGBEE_CONFIGURATION_RESTORE_HAN_PARAMETERS);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Restore_ZigBee_Han_Parameters Restore_UserFile_ID=\"10\"> </Restore_ZigBee_Han_Parameters>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceMessageId.ZIGBEE_CONFIGURATION_READ_STATUS);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Read_ZigBee_Status/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceMessageId.ZIGBEE_CONFIGURATION_CHANGE_HAN_STARTUP_ATTRIBUTE_SETUP);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Change_HAN_SAS HAN_SAS_EXTENDED_PAN_ID=\"A\" HAN_SAS_PAN_ID=\"1\" HAN_SAS_PAN_Channel_Mask=\"2\" HAN_SAS_Insecure_Join=\"true\"> </Change_HAN_SAS>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceMessageId.ZIGBEE_CONFIGURATION_NCP_FIRMWARE_UPDATE_WITH_USER_FILE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<ZIGBEE_NCP_FIRMWARE_UPDATE UserFile_ID=\"10\"> </ZIGBEE_NCP_FIRMWARE_UPDATE>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceMessageId.ZIGBEE_CONFIGURATION_NCP_FIRMWARE_UPDATE_WITH_USER_FILE_AND_ACTIVATE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<ZIGBEE_NCP_FIRMWARE_UPDATE UserFile_ID=\"10\" Activation_date=\"28/10/2013 10:30:00\"> </ZIGBEE_NCP_FIRMWARE_UPDATE>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceMessageId.DEVICE_ACTIONS_REBOOT_DEVICE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Reboot/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<FirmwareUpgrade UserFileID=\"10\"> </FirmwareUpgrade>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_ACTIVATE);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<FirmwareUpgrade UserFileID=\"10\" Activation_date=\"28/10/2013 10:30:00\"> </FirmwareUpgrade>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceMessageId.ADVANCED_TEST_XML_CONFIG);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<XMLConfig><XML>Content</XML></XMLConfig>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceMessageId.ADVANCED_TEST_USERFILE_CONFIG);
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Test_Message Test_File=\"10\"> </Test_Message>", messageEntry.getContent());
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new UkHub();
    }

    @Override
    LegacyMessageConverter doGetMessageConverter() {
        return new UkHubMessageConverter();
    }

    @Override
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        try {
            switch (propertySpec.getName()) {
                case DeviceMessageConstants.NetworkConnectivityIPAddressAttributeName:
                    return "127.0.0.1:80";
                case DeviceMessageConstants.NetworkConnectivityIntervalAttributeName:
                    return "120";
                case DeviceMessageConstants.fromDateAttributeName:
                    return europeanDateTimeFormat.parse("01/10/2013 00:00:00");
                case DeviceMessageConstants.toDateAttributeName:
                    return europeanDateTimeFormat.parse("15/10/2013 00:00:00");
                case DeviceMessageConstants.ZigBeeConfigurationZigBeeAddressAttributeName:
                    return "ABC";
                case DeviceMessageConstants.ZigBeeConfigurationZigBeeLinkKeyAttributeName:
                    return "123";
                case DeviceMessageConstants.ZigBeeConfigurationMirrorAddressAttributeName:
                    return "1";
                case DeviceMessageConstants.ZigBeeConfigurationForceRemovalAttributeName:
                    return new Boolean(false);
                case DeviceMessageConstants.ZigBeeConfigurationHANRestoreUserFileAttributeName:
                case DeviceMessageConstants.ZigBeeConfigurationFirmwareUpdateUserFileAttributeName:
                case DeviceMessageConstants.firmwareUpdateUserFileAttributeName:
                case DeviceMessageConstants.UserFileConfigAttributeName:
                    UserFile mockedUserFile = mock(UserFile.class);
                    when(mockedUserFile.getId()).thenReturn(10);
                    return mockedUserFile;
                case DeviceMessageConstants.ZigBeeConfigurationSASExtendedPanIdAttributeName:
                    return "A";
                case DeviceMessageConstants.ZigBeeConfigurationSASPanIdAttributeName:
                    return "1";
                case DeviceMessageConstants.ZigBeeConfigurationSASPanChannelMaskAttributeName:
                    return 2;
                case DeviceMessageConstants.ZigBeeConfigurationSASInsecureJoinAttributeName:
                    return new Boolean(true);
                case DeviceMessageConstants.firmwareUpdateActivationDateAttributeName:
                case DeviceMessageConstants.ZigBeeConfigurationActivationDateAttributeName:
                    return europeanDateTimeFormat.parse("28/10/2013 10:30:00");
                case DeviceMessageConstants.xmlConfigAttributeName:
                    return "<XML>Content</XML>";
                default:
                    return "0";
            }
        } catch (ParseException e) {
            return "";
        }
    }
}
