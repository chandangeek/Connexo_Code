package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.messages.legacy.LegacyMessageConverter;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.cbo.Password;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdw.core.UserFile;
import com.energyict.protocolimplv2.messages.AdvancedTestMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.LogBookDeviceMessage;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.messages.ZigBeeConfigurationDeviceMessage;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.UkHub;

import java.text.ParseException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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

    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private NlsService nlsService;
    @Mock
    private Converter converter;
    @Mock
    private DeviceMessageFileFinder messageFileFinder;
    @Mock
    private Extractor extractor;

    @Test
    public void testMessageConversion() {
        MessageEntry messageEntry;
        OfflineDeviceMessage offlineDeviceMessage;

        offlineDeviceMessage = createMessage(DeviceActionMessage.DISABLE_WEBSERVER.get(this.propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Disable_Webserver/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceActionMessage.ENABLE_WEBSERVER.get(this.propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Enable_Webserver/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(NetworkConnectivityMessage.ConfigureKeepAliveSettings.get(this.propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<GPRS_Modem_Ping_Setup Ping_IP=\"127.0.0.1:80\" Ping_Interval=\"120\"> </GPRS_Modem_Ping_Setup>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(LogBookDeviceMessage.ReadDebugLogBook.get(this.propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Debug_Logbook From_date=\"01/10/2013 00:00:00\" To_date=\"15/10/2013 00:00:00\"> </Debug_Logbook>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(LogBookDeviceMessage.ReadManufacturerSpecificLogBook.get(this.propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Elster_Specific_Logbook From_date=\"01/10/2013 00:00:00\" To_date=\"15/10/2013 00:00:00\"> </Elster_Specific_Logbook>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ZigBeeConfigurationDeviceMessage.CreateHANNetwork.get(this.propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Create_Han_Network/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ZigBeeConfigurationDeviceMessage.RemoveHANNetwork.get(this.propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Remove_Han_Network/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ZigBeeConfigurationDeviceMessage.JoinZigBeeSlaveDevice.get(this.propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Join_ZigBee_Slave ZigBee_IEEE_Address=\"ABC\" ZigBee_Link_Key=\"123\"> </Join_ZigBee_Slave>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ZigBeeConfigurationDeviceMessage.RemoveMirror.get(this.propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Remove_Mirror Mirror_IEEE_Address=\"1\" Force_Removal=\"false\"> </Remove_Mirror>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ZigBeeConfigurationDeviceMessage.RemoveZigBeeSlaveDevice.get(this.propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Remove_ZigBee_Slave ZigBee_IEEE_Address=\"ABC\"> </Remove_ZigBee_Slave>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ZigBeeConfigurationDeviceMessage.RemoveAllZigBeeSlaveDevices.get(this.propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Remove_All_ZigBee_Slaves/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ZigBeeConfigurationDeviceMessage.BackUpZigBeeHANParameters.get(this.propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Backup_ZigBee_Han_Parameters/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ZigBeeConfigurationDeviceMessage.RestoreZigBeeHANParameters.get(this.propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Restore_ZigBee_Han_Parameters Restore_UserFile_ID=\"10\"> </Restore_ZigBee_Han_Parameters>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ZigBeeConfigurationDeviceMessage.ReadZigBeeStatus.get(this.propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Read_ZigBee_Status/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ZigBeeConfigurationDeviceMessage.ChangeZigBeeHANStartupAttributeSetup.get(this.propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Change_HAN_SAS HAN_SAS_EXTENDED_PAN_ID=\"A\" HAN_SAS_PAN_ID=\"1\" HAN_SAS_PAN_Channel_Mask=\"2\" HAN_SAS_Insecure_Join=\"true\"> </Change_HAN_SAS>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ZigBeeConfigurationDeviceMessage.ZigBeeNCPFirmwareUpdateWithUserFile.get(this.propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<ZIGBEE_NCP_FIRMWARE_UPDATE UserFile_ID=\"10\"> </ZIGBEE_NCP_FIRMWARE_UPDATE>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(ZigBeeConfigurationDeviceMessage.ZigBeeNCPFirmwareUpdateWithUserFileAndActivate.get(this.propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<ZIGBEE_NCP_FIRMWARE_UPDATE UserFile_ID=\"10\" Activation_date=\"28/10/2013 10:30:00\"> </ZIGBEE_NCP_FIRMWARE_UPDATE>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(DeviceActionMessage.REBOOT_DEVICE.get(this.propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Reboot/>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE.get(this.propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<FirmwareUpgrade UserFileID=\"10\"> </FirmwareUpgrade>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE.get(this.propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<FirmwareUpgrade UserFileID=\"10\" Activation_date=\"28/10/2013 10:30:00\"> </FirmwareUpgrade>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(AdvancedTestMessage.XML_CONFIG.get(this.propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<XMLConfig><XML>Content</XML></XMLConfig>", messageEntry.getContent());

        offlineDeviceMessage = createMessage(AdvancedTestMessage.USERFILE_CONFIG.get(this.propertySpecService, this.nlsService, this.converter));
        messageEntry = getMessageConverter().toMessageEntry(offlineDeviceMessage);
        assertEquals("<Test_Message Test_File=\"10\"> </Test_Message>", messageEntry.getContent());
    }

    @Override
    protected Messaging getMessagingProtocol() {
        return new UkHub(propertySpecService, messageFileFinder, extractor);
    }

    @Override
    LegacyMessageConverter doGetMessageConverter() {
        return new UkHubMessageConverter(null, this.propertySpecService, this.nlsService, this.converter, this.extractor);
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
                    return new Password("123");
                case DeviceMessageConstants.ZigBeeConfigurationMirrorAddressAttributeName:
                    return "1";
                case DeviceMessageConstants.ZigBeeConfigurationForceRemovalAttributeName:
                    return Boolean.FALSE;
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
