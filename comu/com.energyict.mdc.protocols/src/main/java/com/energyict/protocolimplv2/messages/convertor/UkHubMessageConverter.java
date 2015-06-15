package com.energyict.protocolimplv2.messages.convertor;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.protocolimpl.generic.messages.GenericMessaging;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.WebRTUFirmwareUpgradeWithUserFileActivationDateMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.WebRTUFirmwareUpgradeWithUserFileMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.XmlConfigMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.OneTagMessageEntry;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a MessageConverter for the legacy IC UkHub protocol.
 *
 * @author sva
 * @since 25/10/13 - 9:35
 */
public class UkHubMessageConverter extends AbstractMessageConverter {

    /**
     * Default constructor for at-runtime instantiation
     */
    public UkHubMessageConverter() {
        super();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.fromDateAttributeName:
            case DeviceMessageConstants.toDateAttributeName:
            case DeviceMessageConstants.firmwareUpdateActivationDateAttributeName:
            case DeviceMessageConstants.ZigBeeConfigurationActivationDateAttributeName:
                return europeanDateTimeFormat.format((Date) messageAttribute);
            case DeviceMessageConstants.UserFileConfigAttributeName:
            case DeviceMessageConstants.firmwareUpdateFileAttributeName:
            case DeviceMessageConstants.ZigBeeConfigurationHANRestoreUserFileAttributeName:
            case DeviceMessageConstants.ZigBeeConfigurationFirmwareUpdateUserFileAttributeName:
                FirmwareVersion firmwareVersion = ((FirmwareVersion) messageAttribute);
                return GenericMessaging.zipAndB64EncodeContent(firmwareVersion.getFirmwareFile());  //Bytes of the firmwareFile as string
            default:
                return messageAttribute.toString();
        }
    }

    protected Map<DeviceMessageId, MessageEntryCreator> getRegistry() {
        Map<DeviceMessageId, MessageEntryCreator> registry = new HashMap<>();
        // Webserver
        registry.put(DeviceMessageId.DEVICE_ACTIONS_DISABLE_WEBSERVER, new OneTagMessageEntry("Disable_Webserver"));
        registry.put(DeviceMessageId.DEVICE_ACTIONS_ENABLE_WEBSERVER, new OneTagMessageEntry("Enable_Webserver"));

        // GPRS Modem Ping setup
        registry.put(DeviceMessageId.NETWORK_CONNECTIVITY_CONFIGURE_KEEP_ALIVE_SETTINGS, new MultipleAttributeMessageEntry("GPRS_Modem_Ping_Setup", "Ping_IP", "Ping_Interval"));

        // Logbooks
        registry.put(DeviceMessageId.LOG_BOOK_READ_DEBUG, new MultipleAttributeMessageEntry("Debug_Logbook", "From_date", "To_date"));
        registry.put(DeviceMessageId.LOG_BOOK_READ_MANUFACTURER_SPECIFIC, new MultipleAttributeMessageEntry("Elster_Specific_Logbook", "From_date", "To_date"));

        //ZigBee setup
        registry.put(DeviceMessageId.ZIGBEE_CONFIGURATION_CREATE_HAN_NETWORK, new OneTagMessageEntry("Create_Han_Network"));
        registry.put(DeviceMessageId.ZIGBEE_CONFIGURATION_REMOVE_HAN_NETWORK, new OneTagMessageEntry("Remove_Han_Network"));
        registry.put(DeviceMessageId.ZIGBEE_CONFIGURATION_JOIN_SLAVE_DEVICE, new MultipleAttributeMessageEntry("Join_ZigBee_Slave", "ZigBee_IEEE_Address", "ZigBee_Link_Key"));
        registry.put(DeviceMessageId.ZIGBEE_CONFIGURATION_REMOVE_MIRROR, new MultipleAttributeMessageEntry("Remove_Mirror", "Mirror_IEEE_Address", "Force_Removal"));
        registry.put(DeviceMessageId.ZIGBEE_CONFIGURATION_REMOVE_SLAVE_DEVICE, new MultipleAttributeMessageEntry("Remove_ZigBee_Slave", "ZigBee_IEEE_Address"));
        registry.put(DeviceMessageId.ZIGBEE_CONFIGURATION_REMOVE_ALL_SLAVE_DEVICES, new OneTagMessageEntry("Remove_All_ZigBee_Slaves"));
        registry.put(DeviceMessageId.ZIGBEE_CONFIGURATION_BACK_UP_HAN_PARAMETERS, new OneTagMessageEntry("Backup_ZigBee_Han_Parameters"));
        registry.put(DeviceMessageId.ZIGBEE_CONFIGURATION_RESTORE_HAN_PARAMETERS, new MultipleAttributeMessageEntry("Restore_ZigBee_Han_Parameters", "Restore_UserFile_ID"));
        registry.put(DeviceMessageId.ZIGBEE_CONFIGURATION_READ_STATUS, new OneTagMessageEntry("Read_ZigBee_Status"));
        registry.put(DeviceMessageId.ZIGBEE_CONFIGURATION_CHANGE_HAN_STARTUP_ATTRIBUTE_SETUP, new MultipleAttributeMessageEntry("Change_HAN_SAS", "HAN_SAS_EXTENDED_PAN_ID", "HAN_SAS_PAN_ID", "HAN_SAS_PAN_Channel_Mask", "HAN_SAS_Insecure_Join"));
        registry.put(DeviceMessageId.ZIGBEE_CONFIGURATION_NCP_FIRMWARE_UPDATE_WITH_USER_FILE, new MultipleAttributeMessageEntry("ZIGBEE_NCP_FIRMWARE_UPDATE", "UserFile_ID"));
        registry.put(DeviceMessageId.ZIGBEE_CONFIGURATION_NCP_FIRMWARE_UPDATE_WITH_USER_FILE_AND_ACTIVATE, new MultipleAttributeMessageEntry("ZIGBEE_NCP_FIRMWARE_UPDATE", "UserFile_ID", "Activation_date"));

        //Reboot
        registry.put(DeviceMessageId.DEVICE_ACTIONS_REBOOT_DEVICE, new OneTagMessageEntry("Reboot"));

        // Firmware
        registry.put(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE, new WebRTUFirmwareUpgradeWithUserFileMessageEntry(DeviceMessageConstants.firmwareUpdateFileAttributeName));
        registry.put(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_ACTIVATE_DATE, new WebRTUFirmwareUpgradeWithUserFileActivationDateMessageEntry(DeviceMessageConstants.firmwareUpdateFileAttributeName, DeviceMessageConstants.firmwareUpdateActivationDateAttributeName));

        //XMLConfig
        registry.put(DeviceMessageId.ADVANCED_TEST_XML_CONFIG, new XmlConfigMessageEntry(DeviceMessageConstants.xmlConfigAttributeName));

        //TestMessage
        registry.put(DeviceMessageId.ADVANCED_TEST_USERFILE_CONFIG, new MultipleAttributeMessageEntry("Test_Message", "Test_File"));
        return registry;
    }

}