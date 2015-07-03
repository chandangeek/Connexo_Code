package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cbo.Password;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdw.core.UserFile;
import com.energyict.protocolimplv2.messages.*;
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
     * Represents a mapping between {@link com.energyict.mdc.messages.DeviceMessageSpec deviceMessageSpecs}
     * and the corresponding {@link com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator}
     */
    private static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>();

    static {
        // Webserver
        registry.put(DeviceActionMessage.DISABLE_WEBSERVER, new OneTagMessageEntry("Disable_Webserver"));
        registry.put(DeviceActionMessage.ENABLE_WEBSERVER, new OneTagMessageEntry("Enable_Webserver"));

        // GPRS Modem Ping setup
        registry.put(NetworkConnectivityMessage.ConfigureKeepAliveSettings, new MultipleAttributeMessageEntry("GPRS_Modem_Ping_Setup", "Ping_IP", "Ping_Interval"));

        // Logbooks
        registry.put(LogBookDeviceMessage.ReadDebugLogBook, new MultipleAttributeMessageEntry("Debug_Logbook", "From_date", "To_date"));
        registry.put(LogBookDeviceMessage.ReadManufacturerSpecificLogBook, new MultipleAttributeMessageEntry("Elster_Specific_Logbook", "From_date", "To_date"));

        //ZigBee setup
        registry.put(ZigBeeConfigurationDeviceMessage.CreateHANNetwork, new OneTagMessageEntry("Create_Han_Network"));
        registry.put(ZigBeeConfigurationDeviceMessage.RemoveHANNetwork, new OneTagMessageEntry("Remove_Han_Network"));
        registry.put(ZigBeeConfigurationDeviceMessage.JoinZigBeeSlaveDevice, new MultipleAttributeMessageEntry("Join_ZigBee_Slave", "ZigBee_IEEE_Address", "ZigBee_Link_Key"));
        registry.put(ZigBeeConfigurationDeviceMessage.RemoveMirror, new MultipleAttributeMessageEntry("Remove_Mirror", "Mirror_IEEE_Address", "Force_Removal"));
        registry.put(ZigBeeConfigurationDeviceMessage.RemoveZigBeeSlaveDevice, new MultipleAttributeMessageEntry("Remove_ZigBee_Slave", "ZigBee_IEEE_Address"));
        registry.put(ZigBeeConfigurationDeviceMessage.RemoveAllZigBeeSlaveDevices, new OneTagMessageEntry("Remove_All_ZigBee_Slaves"));
        registry.put(ZigBeeConfigurationDeviceMessage.BackUpZigBeeHANParameters, new OneTagMessageEntry("Backup_ZigBee_Han_Parameters"));
        registry.put(ZigBeeConfigurationDeviceMessage.RestoreZigBeeHANParameters, new MultipleAttributeMessageEntry("Restore_ZigBee_Han_Parameters", "Restore_UserFile_ID"));
        registry.put(ZigBeeConfigurationDeviceMessage.ReadZigBeeStatus, new OneTagMessageEntry("Read_ZigBee_Status"));
        registry.put(ZigBeeConfigurationDeviceMessage.ChangeZigBeeHANStartupAttributeSetup, new MultipleAttributeMessageEntry("Change_HAN_SAS", "HAN_SAS_EXTENDED_PAN_ID", "HAN_SAS_PAN_ID", "HAN_SAS_PAN_Channel_Mask", "HAN_SAS_Insecure_Join"));
        registry.put(ZigBeeConfigurationDeviceMessage.ZigBeeNCPFirmwareUpdateWithUserFile, new MultipleAttributeMessageEntry("ZIGBEE_NCP_FIRMWARE_UPDATE", "UserFile_ID"));
        registry.put(ZigBeeConfigurationDeviceMessage.ZigBeeNCPFirmwareUpdateWithUserFileAndActivate, new MultipleAttributeMessageEntry("ZIGBEE_NCP_FIRMWARE_UPDATE", "UserFile_ID", "Activation_date"));

        //Reboot
        registry.put(DeviceActionMessage.REBOOT_DEVICE, new OneTagMessageEntry("Reboot"));

        // Firmware
        registry.put(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE,
                new WebRTUFirmwareUpgradeWithUserFileMessageEntry(DeviceMessageConstants.firmwareUpdateUserFileAttributeName));
        registry.put(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE,
                new WebRTUFirmwareUpgradeWithUserFileActivationDateMessageEntry(DeviceMessageConstants.firmwareUpdateUserFileAttributeName, DeviceMessageConstants.firmwareUpdateActivationDateAttributeName));

        //XMLConfig
        registry.put(AdvancedTestMessage.XML_CONFIG, new XmlConfigMessageEntry(DeviceMessageConstants.xmlConfigAttributeName));

        //TestMessage
        registry.put(AdvancedTestMessage.USERFILE_CONFIG, new MultipleAttributeMessageEntry("Test_Message", "Test_File"));
    }

    /**
     * Default constructor for at-runtime instantiation
     */
    public UkHubMessageConverter() {
        super();
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
                return Integer.toString(((UserFile) messageAttribute).getId());
            default:
                return messageAttribute.toString();
        }
    }

    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return registry;
    }
}
