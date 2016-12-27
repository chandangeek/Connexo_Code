package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.Password;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.AdvancedTestMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.LogBookDeviceMessage;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.messages.ZigBeeConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.WebRTUFirmwareUpgradeWithUserFileActivationDateMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.WebRTUFirmwareUpgradeWithUserFileMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.XmlConfigMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.OneTagMessageEntry;
import com.google.common.collect.ImmutableMap;

import java.util.Date;
import java.util.Map;

/**
 * Represents a MessageConverter for the legacy IC UkHub protocol.
 *
 * @author sva
 * @since 25/10/13 - 9:35
 */
public class UkHubMessageConverter extends AbstractMessageConverter {

    public UkHubMessageConverter(Messaging messagingProtocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, Extractor extractor) {
        super(messagingProtocol, propertySpecService, nlsService, converter, extractor);
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.ZigBeeConfigurationZigBeeLinkKeyAttributeName:
                return ((Password) messageAttribute).getValue();
            case DeviceMessageConstants.fromDateAttributeName:
            case DeviceMessageConstants.toDateAttributeName:
            case DeviceMessageConstants.firmwareUpdateActivationDateAttributeName:
            case DeviceMessageConstants.ZigBeeConfigurationActivationDateAttributeName:
                return europeanDateTimeFormat.format((Date) messageAttribute);
            case DeviceMessageConstants.UserFileConfigAttributeName:
            case DeviceMessageConstants.firmwareUpdateUserFileAttributeName:
            case DeviceMessageConstants.ZigBeeConfigurationHANRestoreUserFileAttributeName:
            case DeviceMessageConstants.ZigBeeConfigurationFirmwareUpdateUserFileAttributeName:
                return this.getExtractor().id((DeviceMessageFile) messageAttribute);
            default:
                return messageAttribute.toString();
        }
    }

    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return ImmutableMap
                .<DeviceMessageSpec, MessageEntryCreator>builder()
                // Webserver
                .put(messageSpec(DeviceActionMessage.DISABLE_WEBSERVER), new OneTagMessageEntry("Disable_Webserver"))
                .put(messageSpec(DeviceActionMessage.ENABLE_WEBSERVER), new OneTagMessageEntry("Enable_Webserver"))

                // GPRS Modem Ping setup
                .put(messageSpec(NetworkConnectivityMessage.ConfigureKeepAliveSettings), new MultipleAttributeMessageEntry("GPRS_Modem_Ping_Setup", "Ping_IP", "Ping_Interval"))

                // Logbooks
                .put(messageSpec(LogBookDeviceMessage.ReadDebugLogBook), new MultipleAttributeMessageEntry("Debug_Logbook", "From_date", "To_date"))
                .put(messageSpec(LogBookDeviceMessage.ReadManufacturerSpecificLogBook), new MultipleAttributeMessageEntry("Elster_Specific_Logbook", "From_date", "To_date"))

                //ZigBee setup
                .put(messageSpec(ZigBeeConfigurationDeviceMessage.CreateHANNetwork), new OneTagMessageEntry("Create_Han_Network"))
                .put(messageSpec(ZigBeeConfigurationDeviceMessage.RemoveHANNetwork), new OneTagMessageEntry("Remove_Han_Network"))
                .put(messageSpec(ZigBeeConfigurationDeviceMessage.JoinZigBeeSlaveDevice), new MultipleAttributeMessageEntry("Join_ZigBee_Slave", "ZigBee_IEEE_Address", "ZigBee_Link_Key"))
                .put(messageSpec(ZigBeeConfigurationDeviceMessage.RemoveMirror), new MultipleAttributeMessageEntry("Remove_Mirror", "Mirror_IEEE_Address", "Force_Removal"))
                .put(messageSpec(ZigBeeConfigurationDeviceMessage.RemoveZigBeeSlaveDevice), new MultipleAttributeMessageEntry("Remove_ZigBee_Slave", "ZigBee_IEEE_Address"))
                .put(messageSpec(ZigBeeConfigurationDeviceMessage.RemoveAllZigBeeSlaveDevices), new OneTagMessageEntry("Remove_All_ZigBee_Slaves"))
                .put(messageSpec(ZigBeeConfigurationDeviceMessage.BackUpZigBeeHANParameters), new OneTagMessageEntry("Backup_ZigBee_Han_Parameters"))
                .put(messageSpec(ZigBeeConfigurationDeviceMessage.RestoreZigBeeHANParameters), new MultipleAttributeMessageEntry("Restore_ZigBee_Han_Parameters", "Restore_UserFile_ID"))
                .put(messageSpec(ZigBeeConfigurationDeviceMessage.ReadZigBeeStatus), new OneTagMessageEntry("Read_ZigBee_Status"))
                .put(messageSpec(ZigBeeConfigurationDeviceMessage.ChangeZigBeeHANStartupAttributeSetup), new MultipleAttributeMessageEntry("Change_HAN_SAS", "HAN_SAS_EXTENDED_PAN_ID", "HAN_SAS_PAN_ID", "HAN_SAS_PAN_Channel_Mask", "HAN_SAS_Insecure_Join"))
                .put(messageSpec(ZigBeeConfigurationDeviceMessage.ZigBeeNCPFirmwareUpdateWithUserFile), new MultipleAttributeMessageEntry("ZIGBEE_NCP_FIRMWARE_UPDATE", "UserFile_ID"))
                .put(messageSpec(ZigBeeConfigurationDeviceMessage.ZigBeeNCPFirmwareUpdateWithUserFileAndActivate), new MultipleAttributeMessageEntry("ZIGBEE_NCP_FIRMWARE_UPDATE", "UserFile_ID", "Activation_date"))

                //Reboot
                .put(messageSpec(DeviceActionMessage.REBOOT_DEVICE), new OneTagMessageEntry("Reboot"))

                // Firmware
                .put(messageSpec(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE), new WebRTUFirmwareUpgradeWithUserFileMessageEntry(DeviceMessageConstants.firmwareUpdateUserFileAttributeName))
                .put(messageSpec(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE), new WebRTUFirmwareUpgradeWithUserFileActivationDateMessageEntry(DeviceMessageConstants.firmwareUpdateUserFileAttributeName, DeviceMessageConstants.firmwareUpdateActivationDateAttributeName))

                //XMLConfig
                .put(messageSpec(AdvancedTestMessage.XML_CONFIG), new XmlConfigMessageEntry(DeviceMessageConstants.xmlConfigAttributeName))

                //TestMessage
                .put(messageSpec(AdvancedTestMessage.USERFILE_CONFIG), new MultipleAttributeMessageEntry("Test_Message", "Test_File"))
                .build();
    }
}
