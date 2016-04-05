package com.energyict.protocolimplv2.nta.dsmr40.messages;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;
import com.energyict.protocolimplv2.nta.dsmr23.messages.Dsmr23Messaging;

import java.util.List;

/**
 * @author sva
 * @since 6/01/2015 - 13:30
 */
public class Dsmr40Messaging extends Dsmr23Messaging {

    public Dsmr40Messaging(AbstractMessageExecutor messageExecutor) {
        super(messageExecutor);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        List<DeviceMessageSpec> supportedMessages = super.getSupportedMessages();

        // firmware upgrade related - add message with additional attribute 'Image identifier'
        supportedMessages.add(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_IMAGE_IDENTIFIER);
        supportedMessages.add(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE_AND_IMAGE_IDENTIFIER);

        // Configuration change
        supportedMessages.add(ConfigurationChangeDeviceMessage.ChangeAdministrativeStatus);

        if (supportMeterReset) {
            supportedMessages.add(DeviceActionMessage.RESTORE_FACTORY_SETTINGS);
        }
        if (supportMBus) {
            supportedMessages.add(ConfigurationChangeDeviceMessage.DISABLE_DISCOVERY_ON_POWER_UP);
            supportedMessages.add(ConfigurationChangeDeviceMessage.ENABLE_DISCOVERY_ON_POWER_UP);
        }

        // security related
        supportedMessages.add(SecurityMessage.DISABLE_DLMS_AUTHENTICATION_LEVEL_P0);
        supportedMessages.add(SecurityMessage.DISABLE_DLMS_AUTHENTICATION_LEVEL_P3);
        supportedMessages.add(SecurityMessage.ENABLE_DLMS_AUTHENTICATION_LEVEL_P0);
        supportedMessages.add(SecurityMessage.ENABLE_DLMS_AUTHENTICATION_LEVEL_P3);

        // Dsmr 2.3 security related messages not supported in Dmsr 4.0
        supportedMessages.remove(SecurityMessage.CHANGE_DLMS_AUTHENTICATION_LEVEL);

        return supportedMessages;
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        return super.format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return super.executePendingMessages(pendingMessages);
    }
}
