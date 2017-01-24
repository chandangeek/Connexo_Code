package com.energyict.protocolimplv2.nta.dsmr40.messages;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;
import com.energyict.protocolimplv2.nta.dsmr23.messages.Dsmr23Messaging;

import java.util.List;
import java.util.Set;

/**
 * @author sva
 * @since 6/01/2015 - 13:30
 */
public class Dsmr40Messaging extends Dsmr23Messaging {

    public Dsmr40Messaging(AbstractMessageExecutor messageExecutor, TopologyService topologyService) {
        super(messageExecutor, topologyService);
    }

    @Override
    public Set<DeviceMessageId> getSupportedMessages() {
        Set<DeviceMessageId> supportedMessages = super.getSupportedMessages();

        // firmware upgrade related - add message with additional attribute 'Image identifier'
//        supportedMessages.add(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_IMAGE_IDENTIFIER);
//        supportedMessages.add(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE_AND_IMAGE_IDENTIFIER);
        supportedMessages.add(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE);
        supportedMessages.add(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_ACTIVATE_DATE);

        // Configuration change
        supportedMessages.add(DeviceMessageId.CONFIGURATION_CHANGE_CHANGE_ADMINISTRATIVE_STATUS);

        if (supportMeterReset) {
            supportedMessages.add(DeviceMessageId.DEVICE_ACTIONS_RESTORE_FACTORY_SETTINGS);
        }
        if (supportMBus) {
            supportedMessages.add(DeviceMessageId.CONFIGURATION_CHANGE_DISABLE_DISCOVERY_ON_POWER_UP);
            supportedMessages.add(DeviceMessageId.CONFIGURATION_CHANGE_ENABLE_DISCOVERY_ON_POWER_UP);
        }

        // security related
        supportedMessages.add(DeviceMessageId.SECURITY_DISABLE_DLMS_AUTHENTICATION_LEVEL_P0);
        supportedMessages.add(DeviceMessageId.SECURITY_DISABLE_DLMS_AUTHENTICATION_LEVEL_P3);
        supportedMessages.add(DeviceMessageId.SECURITY_ENABLE_DLMS_AUTHENTICATION_LEVEL_P0);
        supportedMessages.add(DeviceMessageId.SECURITY_ENABLE_DLMS_AUTHENTICATION_LEVEL_P3);

        // Dsmr 2.3 security related messages not supported in Dmsr 4.0
        supportedMessages.remove(DeviceMessageId.SECURITY_CHANGE_DLMS_AUTHENTICATION_LEVEL);

        return supportedMessages;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return super.format(propertySpec, messageAttribute);
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return super.executePendingMessages(pendingMessages);
    }
}
