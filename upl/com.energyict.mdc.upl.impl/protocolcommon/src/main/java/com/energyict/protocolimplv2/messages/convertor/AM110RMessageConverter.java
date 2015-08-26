package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cbo.Password;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdw.core.UserFile;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.*;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.WebRTUFirmwareUpgradeWithUserFileActivationDateMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.WebRTUFirmwareUpgradeWithUserFileMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleInnerTagsMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.OneTagMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagMessageEntry;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cisac on 8/6/2015.
 */
public class AM110RMessageConverter extends AbstractMessageConverter {

    /**
     * Represents a mapping between {@link com.energyict.mdc.messages.DeviceMessageSpec deviceMessageSpecs}
     * and the corresponding {@link com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator}
     */
    protected static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>();

    static {

        //GPRS modem setup category
        registry.put(NetworkConnectivityMessage.SetAutoConnectMode, new MultipleAttributeMessageEntry(RtuMessageConstant.CONNECTION_MODE, RtuMessageConstant.CONNECT_MODE));
        registry.put(NetworkConnectivityMessage.WakeupParameters, new MultipleAttributeMessageEntry(RtuMessageConstant.WAKEUP_PARAMETERS, RtuMessageConstant.WAKEUP_CALLING_WINDOW_LENGTH, RtuMessageConstant.WAKEUP_IDLE_TIMEOUT));
        //TODO: implement it with optional parameters
        registry.put(NetworkConnectivityMessage.PreferredNetworkOperatorList, new MultipleAttributeMessageEntry(RtuMessageConstant.PREFERRED_NETWORK_OPERATORS_LIST,
                RtuMessageConstant.NETWORK_OPERATOR + "_1",
                RtuMessageConstant.NETWORK_OPERATOR + "_2",
                RtuMessageConstant.NETWORK_OPERATOR + "_3",
                RtuMessageConstant.NETWORK_OPERATOR + "_4",
                RtuMessageConstant.NETWORK_OPERATOR + "_5",
                RtuMessageConstant.NETWORK_OPERATOR + "_6",
                RtuMessageConstant.NETWORK_OPERATOR + "_7",
                RtuMessageConstant.NETWORK_OPERATOR + "_8",
                RtuMessageConstant.NETWORK_OPERATOR + "_9",
                RtuMessageConstant.NETWORK_OPERATOR + "_10"));

        // Logbooks
        registry.put(LogBookDeviceMessage.ReadDebugLogBook, new MultipleAttributeMessageEntry(RtuMessageConstant.DEBUG_LOGBOOK, RtuMessageConstant.LOGBOOK_FROM, RtuMessageConstant.LOGBOOK_TO));
        registry.put(LogBookDeviceMessage.ReadManufacturerSpecificLogBook, new MultipleAttributeMessageEntry(RtuMessageConstant.ELSTER_SPECIFIC_LOGBOOK, RtuMessageConstant.LOGBOOK_FROM, RtuMessageConstant.LOGBOOK_TO));

        // Webserver
        registry.put(DeviceActionMessage.DISABLE_WEBSERVER, new OneTagMessageEntry(RtuMessageConstant.WEBSERVER_DISABLE));
        registry.put(DeviceActionMessage.ENABLE_WEBSERVER, new OneTagMessageEntry(RtuMessageConstant.WEBSERVER_ENABLE));

        //MessageCategory containing relevant HAN management messages
        registry.put(ZigBeeConfigurationDeviceMessage.CreateHANNetwork, new SimpleTagMessageEntry(RtuMessageConstant.CREATE_HAN_NETWORK));
        registry.put(ZigBeeConfigurationDeviceMessage.RemoveHANNetwork, new SimpleTagMessageEntry(RtuMessageConstant.REMOVE_HAN_NETWORK));
        registry.put(ZigBeeConfigurationDeviceMessage.BackUpZigBeeHANParameters, new SimpleTagMessageEntry(RtuMessageConstant.BACKUP_ZIGBEE_HAN_PARAMETERS));
        registry.put(ZigBeeConfigurationDeviceMessage.RestoreZigBeeHANParameters, new MultipleAttributeMessageEntry(RtuMessageConstant.RESTORE_ZIGBEE_HAN_PARAMETERS, RtuMessageConstant.RESTORE_ZIGBEE_PARAMETERS_USERFILE_ID));
        registry.put(ZigBeeConfigurationDeviceMessage.JoinZigBeeSlaveFromDeviceType, new MultipleAttributeMessageEntry(RtuMessageConstant.JOIN_ZIGBEE_SLAVE_FROM_DEVICE_TYPE, RtuMessageConstant.JOIN_ZIGBEE_SLAVE_IEEE_ADDRESS, RtuMessageConstant.JOIN_ZIGBEE_SLAVE_LINK_KEY, RtuMessageConstant.JOIN_ZIGBEE_SLAVE_DEVICE_TYPE));
        registry.put(ZigBeeConfigurationDeviceMessage.RemoveZigBeeSlaveDevice, new MultipleAttributeMessageEntry(RtuMessageConstant.REMOVE_ZIGBEE_SLAVE, RtuMessageConstant.REMOVE_ZIGBEE_SLAVE_IEEE_ADDRESS));
        registry.put(ZigBeeConfigurationDeviceMessage.RemoveAllZigBeeSlaveDevices, new SimpleTagMessageEntry(RtuMessageConstant.REMOVE_ALL_ZIGBEE_SLAVES));
        registry.put(ZigBeeConfigurationDeviceMessage.RemoveMirror, new MultipleAttributeMessageEntry(RtuMessageConstant.REMOVE_ZIGBEE_MIRROR, RtuMessageConstant.REMOVE_ZIGBEE_MIRROR_IEEE_ADDRESS, RtuMessageConstant.REMOVE_ZIGBEE_MIRROR_FORCE));
        registry.put(ZigBeeConfigurationDeviceMessage.UpdateLinkKey, new MultipleAttributeMessageEntry(RtuMessageConstant.UPDATE_HAN_LINK_KEY, RtuMessageConstant.UPDATE_HAN_LINK_KEY_SLAVE_IEEE_ADDRESS));
        registry.put(ZigBeeConfigurationDeviceMessage.ZigBeeNCPFirmwareUpdateWithUserFile, new MultipleAttributeMessageEntry(RtuMessageConstant.ZIGBEE_NCP_FIRMWARE_UPGRADE, RtuMessageConstant.FIRMWARE_USER_FILE));
        registry.put(ZigBeeConfigurationDeviceMessage.ZigBeeNCPFirmwareUpdateWithUserFileAndActivate, new MultipleAttributeMessageEntry(RtuMessageConstant.ZIGBEE_NCP_FIRMWARE_UPGRADE, RtuMessageConstant.FIRMWARE_USER_FILE, RtuMessageConstant.ACTIVATE_DATE));

        // Firmware
        registry.put(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE,
                new MultipleAttributeMessageEntry(RtuMessageConstant.FIRMWARE_UPGRADE, RtuMessageConstant.FIRMWARE_USER_FILE));
        registry.put(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE,
                new MultipleAttributeMessageEntry(RtuMessageConstant.FIRMWARE_UPGRADE, RtuMessageConstant.FIRMWARE_USER_FILE, RtuMessageConstant.ACTIVATE_DATE));

    }

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return registry;
    }

    public AM110RMessageConverter(){
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
            case DeviceMessageConstants.firmwareUpdateUserFileAttributeName:
            case DeviceMessageConstants.ZigBeeConfigurationFirmwareUpdateUserFileAttributeName:
                return new String(((UserFile) messageAttribute).loadFileInByteArray(), Charset.forName("UTF-8"));   // We suppose the UserFile contains regular ASCII
            case DeviceMessageConstants.ZigBeeConfigurationHANRestoreUserFileAttributeName:
                return Integer.toString(((UserFile) messageAttribute).getId());
            default:
                return messageAttribute.toString();
        }
    }
}
