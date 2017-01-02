package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.Password;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.LogBookDeviceMessage;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.messages.ZigBeeConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.OneTagMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagMessageEntry;
import com.google.common.collect.ImmutableMap;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.Map;

/**
 * Created by cisac on 8/6/2015.
 */
public class AM110RMessageConverter extends AbstractMessageConverter {

    private final DeviceMessageFileExtractor deviceMessageFileExtractor;

    public AM110RMessageConverter(Messaging messagingProtocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, DeviceMessageFileExtractor deviceMessageFileExtractor) {
        super(messagingProtocol, propertySpecService, nlsService, converter);
        this.deviceMessageFileExtractor = deviceMessageFileExtractor;
    }

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return ImmutableMap
                .<DeviceMessageSpec, MessageEntryCreator>builder()
                //GPRS modem setup category
                .put(messageSpec(NetworkConnectivityMessage.SetAutoConnectMode), new MultipleAttributeMessageEntry(RtuMessageConstant.CONNECTION_MODE, RtuMessageConstant.CONNECT_MODE))
                .put(messageSpec(NetworkConnectivityMessage.WakeupParameters), new MultipleAttributeMessageEntry(RtuMessageConstant.WAKEUP_PARAMETERS, RtuMessageConstant.WAKEUP_CALLING_WINDOW_LENGTH, RtuMessageConstant.WAKEUP_IDLE_TIMEOUT))
                //TODO: implement it with optional parameters
                .put(messageSpec(NetworkConnectivityMessage.PreferredNetworkOperatorList),
                        new MultipleAttributeMessageEntry(
                                RtuMessageConstant.PREFERRED_NETWORK_OPERATORS_LIST,
                                RtuMessageConstant.NETWORK_OPERATOR + "_1",
                                RtuMessageConstant.NETWORK_OPERATOR + "_2",
                                RtuMessageConstant.NETWORK_OPERATOR + "_3",
                                RtuMessageConstant.NETWORK_OPERATOR + "_4",
                                RtuMessageConstant.NETWORK_OPERATOR + "_5",
                                RtuMessageConstant.NETWORK_OPERATOR + "_6",
                                RtuMessageConstant.NETWORK_OPERATOR + "_7",
                                RtuMessageConstant.NETWORK_OPERATOR + "_8",
                                RtuMessageConstant.NETWORK_OPERATOR + "_9",
                                RtuMessageConstant.NETWORK_OPERATOR + "_10"))

                // Logbooks
                .put(messageSpec(LogBookDeviceMessage.ReadDebugLogBook), new MultipleAttributeMessageEntry(RtuMessageConstant.DEBUG_LOGBOOK, RtuMessageConstant.LOGBOOK_FROM, RtuMessageConstant.LOGBOOK_TO))
                .put(messageSpec(LogBookDeviceMessage.ReadManufacturerSpecificLogBook), new MultipleAttributeMessageEntry(RtuMessageConstant.ELSTER_SPECIFIC_LOGBOOK, RtuMessageConstant.LOGBOOK_FROM, RtuMessageConstant.LOGBOOK_TO))

                // Webserver
                .put(messageSpec(DeviceActionMessage.DISABLE_WEBSERVER), new OneTagMessageEntry(RtuMessageConstant.WEBSERVER_DISABLE))
                .put(messageSpec(DeviceActionMessage.ENABLE_WEBSERVER), new OneTagMessageEntry(RtuMessageConstant.WEBSERVER_ENABLE))

                //MessageCategory containing relevant HAN management messages
                .put(messageSpec(ZigBeeConfigurationDeviceMessage.CreateHANNetwork), new SimpleTagMessageEntry(RtuMessageConstant.CREATE_HAN_NETWORK))
                .put(messageSpec(ZigBeeConfigurationDeviceMessage.RemoveHANNetwork), new SimpleTagMessageEntry(RtuMessageConstant.REMOVE_HAN_NETWORK))
                .put(messageSpec(ZigBeeConfigurationDeviceMessage.BackUpZigBeeHANParameters), new SimpleTagMessageEntry(RtuMessageConstant.BACKUP_ZIGBEE_HAN_PARAMETERS))
                .put(messageSpec(ZigBeeConfigurationDeviceMessage.RestoreZigBeeHANParameters), new MultipleAttributeMessageEntry(RtuMessageConstant.RESTORE_ZIGBEE_HAN_PARAMETERS, RtuMessageConstant.RESTORE_ZIGBEE_PARAMETERS_USERFILE_ID))
                .put(messageSpec(ZigBeeConfigurationDeviceMessage.JoinZigBeeSlaveFromDeviceType), new MultipleAttributeMessageEntry(RtuMessageConstant.JOIN_ZIGBEE_SLAVE_FROM_DEVICE_TYPE, RtuMessageConstant.JOIN_ZIGBEE_SLAVE_IEEE_ADDRESS, RtuMessageConstant.JOIN_ZIGBEE_SLAVE_LINK_KEY, RtuMessageConstant.JOIN_ZIGBEE_SLAVE_DEVICE_TYPE))
                .put(messageSpec(ZigBeeConfigurationDeviceMessage.RemoveZigBeeSlaveDevice), new MultipleAttributeMessageEntry(RtuMessageConstant.REMOVE_ZIGBEE_SLAVE, RtuMessageConstant.REMOVE_ZIGBEE_SLAVE_IEEE_ADDRESS))
                .put(messageSpec(ZigBeeConfigurationDeviceMessage.RemoveAllZigBeeSlaveDevices), new SimpleTagMessageEntry(RtuMessageConstant.REMOVE_ALL_ZIGBEE_SLAVES))
                .put(messageSpec(ZigBeeConfigurationDeviceMessage.RemoveMirror), new MultipleAttributeMessageEntry(RtuMessageConstant.REMOVE_ZIGBEE_MIRROR, RtuMessageConstant.REMOVE_ZIGBEE_MIRROR_IEEE_ADDRESS, RtuMessageConstant.REMOVE_ZIGBEE_MIRROR_FORCE))
                .put(messageSpec(ZigBeeConfigurationDeviceMessage.UpdateLinkKey), new MultipleAttributeMessageEntry(RtuMessageConstant.UPDATE_HAN_LINK_KEY, RtuMessageConstant.UPDATE_HAN_LINK_KEY_SLAVE_IEEE_ADDRESS))
                .put(messageSpec(ZigBeeConfigurationDeviceMessage.ZigBeeNCPFirmwareUpdateWithUserFile), new MultipleAttributeMessageEntry(RtuMessageConstant.ZIGBEE_NCP_FIRMWARE_UPGRADE, RtuMessageConstant.FIRMWARE_USER_FILE))
                .put(messageSpec(ZigBeeConfigurationDeviceMessage.ZigBeeNCPFirmwareUpdateWithUserFileAndActivate), new MultipleAttributeMessageEntry(RtuMessageConstant.ZIGBEE_NCP_FIRMWARE_UPGRADE, RtuMessageConstant.FIRMWARE_USER_FILE, RtuMessageConstant.ACTIVATE_DATE))

                // Firmware
                .put(messageSpec(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE), new MultipleAttributeMessageEntry(RtuMessageConstant.FIRMWARE_UPGRADE, RtuMessageConstant.FIRMWARE_USER_FILE))
                .put(messageSpec(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE), new MultipleAttributeMessageEntry(RtuMessageConstant.FIRMWARE_UPGRADE, RtuMessageConstant.FIRMWARE_USER_FILE, RtuMessageConstant.ACTIVATE_DATE))
                .build();
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
                return this.deviceMessageFileExtractor.contents((DeviceMessageFile) messageAttribute, Charset.forName("UTF-8"));
            case DeviceMessageConstants.ZigBeeConfigurationHANRestoreUserFileAttributeName:
                return this.deviceMessageFileExtractor.id((DeviceMessageFile) messageAttribute);
            default:
                return messageAttribute.toString();
        }
    }
}
