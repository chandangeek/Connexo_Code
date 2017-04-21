package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.LegacyMessageConverter;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.protocolimplv2.eict.eiweb.SimplePassword;
import com.energyict.protocolimplv2.messages.AdvancedTestMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.LogBookDeviceMessage;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.messages.ZigBeeConfigurationDeviceMessage;
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
public class UkHubMessageConverterTest extends AbstractV2MessageConverterTest {

    @Test
    public void testMessageConversion() {
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;

        offlineDeviceMessage = createMessage(DeviceActionMessage.DISABLE_WEBSERVER.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Disable_Webserver/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceActionMessage.ENABLE_WEBSERVER.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Enable_Webserver/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(NetworkConnectivityMessage.ConfigureKeepAliveSettings.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<GPRS_Modem_Ping_Setup Ping_IP=\"127.0.0.1:80\" Ping_Interval=\"120\"> </GPRS_Modem_Ping_Setup>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(LogBookDeviceMessage.ReadDebugLogBook.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Debug_Logbook From_date=\"01/10/2013 00:00:00\" To_date=\"15/10/2013 00:00:00\"> </Debug_Logbook>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(LogBookDeviceMessage.ReadManufacturerSpecificLogBook.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Elster_Specific_Logbook From_date=\"01/10/2013 00:00:00\" To_date=\"15/10/2013 00:00:00\"> </Elster_Specific_Logbook>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ZigBeeConfigurationDeviceMessage.CreateHANNetwork.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Create_Han_Network/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ZigBeeConfigurationDeviceMessage.RemoveHANNetwork.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Remove_Han_Network/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ZigBeeConfigurationDeviceMessage.JoinZigBeeSlaveDevice.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Join_ZigBee_Slave ZigBee_IEEE_Address=\"ABC\" ZigBee_Link_Key=\"123\"> </Join_ZigBee_Slave>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ZigBeeConfigurationDeviceMessage.RemoveMirror.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Remove_Mirror Mirror_IEEE_Address=\"1\" Force_Removal=\"false\"> </Remove_Mirror>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ZigBeeConfigurationDeviceMessage.RemoveZigBeeSlaveDevice.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Remove_ZigBee_Slave ZigBee_IEEE_Address=\"ABC\"> </Remove_ZigBee_Slave>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ZigBeeConfigurationDeviceMessage.RemoveAllZigBeeSlaveDevices.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Remove_All_ZigBee_Slaves/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ZigBeeConfigurationDeviceMessage.BackUpZigBeeHANParameters.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Backup_ZigBee_Han_Parameters/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ZigBeeConfigurationDeviceMessage.RestoreZigBeeHANParameters.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Restore_ZigBee_Han_Parameters Restore_UserFile_ID=\"10\"> </Restore_ZigBee_Han_Parameters>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ZigBeeConfigurationDeviceMessage.ReadZigBeeStatus.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Read_ZigBee_Status/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ZigBeeConfigurationDeviceMessage.ChangeZigBeeHANStartupAttributeSetup.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Change_HAN_SAS HAN_SAS_EXTENDED_PAN_ID=\"A\" HAN_SAS_PAN_ID=\"1\" HAN_SAS_PAN_Channel_Mask=\"2\" HAN_SAS_Insecure_Join=\"true\"> </Change_HAN_SAS>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ZigBeeConfigurationDeviceMessage.ZigBeeNCPFirmwareUpdateWithUserFile.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<ZIGBEE_NCP_FIRMWARE_UPDATE UserFile_ID=\"path\"> </ZIGBEE_NCP_FIRMWARE_UPDATE>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ZigBeeConfigurationDeviceMessage.ZigBeeNCPFirmwareUpdateWithUserFileAndActivate.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<ZIGBEE_NCP_FIRMWARE_UPDATE UserFile_ID=\"path\" Activation_date=\"28/10/2013 10:30:00\"> </ZIGBEE_NCP_FIRMWARE_UPDATE>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceActionMessage.REBOOT_DEVICE.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Reboot/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<FirmwareUpgrade FirmwareFilePath=\"path\"> </FirmwareUpgrade>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<FirmwareUpgrade FirmwareFilePath=\"path\" Activation_date=\"28/10/2013 10:30:00\"> </FirmwareUpgrade>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(AdvancedTestMessage.XML_CONFIG.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<XMLConfig><XML>Content</XML></XMLConfig>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(AdvancedTestMessage.USERFILE_CONFIG.get(propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Test_Message Test_File=\"10\"> </Test_Message>", messageEntry.getContent());
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new UkHub(propertySpecService, deviceMessageFileFinder, deviceMessageFileExtractor);
    }

    @Override
    LegacyMessageConverter doGetMessageConverter() {
        return new UkHubMessageConverter(propertySpecService, this.nlsService, this.converter, this.deviceMessageFileExtractor);
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
                    return new SimplePassword("123");
                case DeviceMessageConstants.ZigBeeConfigurationMirrorAddressAttributeName:
                    return "1";
                case DeviceMessageConstants.ZigBeeConfigurationForceRemovalAttributeName:
                    return Boolean.FALSE;
                case DeviceMessageConstants.ZigBeeConfigurationFirmwareUpdateFileAttributeName:
                case DeviceMessageConstants.firmwareUpdateFileAttributeName:
                    return "path";
                case DeviceMessageConstants.ZigBeeConfigurationHANRestoreUserFileAttributeName:
                case DeviceMessageConstants.UserFileConfigAttributeName:
                    DeviceMessageFile deviceMessageFile = mock(DeviceMessageFile.class);
                    when(deviceMessageFileExtractor.id(deviceMessageFile)).thenReturn("10");
                    return deviceMessageFile;
                case DeviceMessageConstants.ZigBeeConfigurationSASExtendedPanIdAttributeName:
                    return "A";
                case DeviceMessageConstants.ZigBeeConfigurationSASPanIdAttributeName:
                    return "1";
                case DeviceMessageConstants.ZigBeeConfigurationSASPanChannelMaskAttributeName:
                    return 2;
                case DeviceMessageConstants.ZigBeeConfigurationSASInsecureJoinAttributeName:
                    return Boolean.TRUE;
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
