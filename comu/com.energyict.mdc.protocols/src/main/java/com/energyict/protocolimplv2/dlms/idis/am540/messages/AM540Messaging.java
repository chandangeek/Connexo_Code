package com.energyict.protocolimplv2.dlms.idis.am540.messages;


import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.am130.messages.AM130Messaging;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.IDISMessageExecutor;

import java.util.HashSet;
import java.util.Set;

/**
 * @author sva
 * @since 11/08/2015 - 16:14
 */
public class AM540Messaging extends AM130Messaging {

    public AM540Messaging(AbstractDlmsProtocol protocol) {
        super(protocol);
    }


    protected IDISMessageExecutor getMessageExecutor() {
        if (messageExecutor == null) {
            this.messageExecutor = new AM540MessageExecutor(getProtocol(), getProtocol().getIssueService(), getProtocol().getReadingTypeUtilService(), getProtocol().getCollectedDataFactory());
        }
        return messageExecutor;
    }

    @Override
    public Set<DeviceMessageId> getSupportedMessages() {
        if (supportedMessages == null) {
            supportedMessages = new HashSet<>();
            addSupportedDeviceMessages(supportedMessages);
        }
        return supportedMessages;
    }

    @Override
    protected void addSupportedDeviceMessages(Set<DeviceMessageId> supportedMessages) {
        addCommonDeviceMessages(supportedMessages);
        addAlarmConfigurationMessages(supportedMessages);
        addContactorDeviceMessages(supportedMessages);
        addPLCConfigurationDeviceMessages(supportedMessages);
        addAdditionalDeviceMessages(supportedMessages);
//        supportedMessages.add(LoadBalanceDeviceMessage.UPDATE_SUPERVISION_MONITOR);
//        supportedMessages.add(LoadProfileMessage.LOAD_PROFILE_OPT_IN_OUT);
//        supportedMessages.add(LoadProfileMessage.SET_DISPLAY_ON_OFF);
    }

    private void addAdditionalDeviceMessages(Set<DeviceMessageId> supportedMessages) {
//        supportedMessages.add(FirmwareDeviceMessage.VerifyAndActivateFirmware);
//        supportedMessages.add(FirmwareDeviceMessage.ENABLE_IMAGE_TRANSFER);
    }

    @Override
    protected void addAlarmConfigurationMessages(Set<DeviceMessageId> supportedMessages) {
        super.addAlarmConfigurationMessages(supportedMessages);
        supportedMessages.add(DeviceMessageId.ALARM_CONFIGURATION_RESET_ALL_ALARM_BITS);
    }

    private void addPLCConfigurationDeviceMessages(Set<DeviceMessageId> supportedMessages) {
        // PLC configuration - G3-PLC MAC setup
        supportedMessages.add(DeviceMessageId.PLC_CONFIGURATION_SET_TMR_TTL);
        supportedMessages.add(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_FRAME_RETRIES);
        supportedMessages.add(DeviceMessageId.PLC_CONFIGURATION_SET_NEIGHBOUR_TABLE_ENTRY_TTL);
        supportedMessages.add(DeviceMessageId.PLC_CONFIGURATION_SET_HIGH_PRIORITY_WINDOW_SIZE);
        supportedMessages.add(DeviceMessageId.PLC_CONFIGURATION_SET_CSMA_FAIRNESS_LIMIT);
        supportedMessages.add(DeviceMessageId.PLC_CONFIGURATION_SET_BEACON_RANDOMIZATION_WINDOW_LENGTH);
        supportedMessages.add(DeviceMessageId.PLC_CONFIGURATION_SET_MAC_A);
        supportedMessages.add(DeviceMessageId.PLC_CONFIGURATION_SET_MAC_K);
        supportedMessages.add(DeviceMessageId.PLC_CONFIGURATION_SET_MINIMUM_CW_ATTEMPTS);
        supportedMessages.add(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_BE);
        supportedMessages.add(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_CSMA_BACK_OFF);
        supportedMessages.add(DeviceMessageId.PLC_CONFIGURATION_SET_MIN_BE);

        // PLC configuration - G3-PLC MAC 6LoWPAN layer setup
        supportedMessages.add(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_NUMBER_OF_HOPS_ATTRIBUTENAME);
        supportedMessages.add(DeviceMessageId.PLC_CONFIGURATION_SET_WEAK_LQI_VALUE_ATTRIBUTENAME);
        supportedMessages.add(DeviceMessageId.PLC_CONFIGURATION_SET_SECURITY_LEVEL);
        supportedMessages.add(DeviceMessageId.PLC_CONFIGURATION_SET_ROUTING_CONFIGURATION);
        supportedMessages.add(DeviceMessageId.PLC_CONFIGURATION_SET_BROAD_CAST_LOG_TABLE_ENTRY_TTL);
        supportedMessages.add(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_JOIN_WAIT_TIME);
        supportedMessages.add(DeviceMessageId.PLC_CONFIGURATION_SET_PATH_DISCOVERY_TIME);
        supportedMessages.add(DeviceMessageId.PLC_CONFIGURATION_SET_METRIC_TYPE);
        supportedMessages.add(DeviceMessageId.PLC_CONFIGURATION_SET_COORD_SHORT_ADDRESS);
        supportedMessages.add(DeviceMessageId.PLC_CONFIGURATION_SET_DISABLE_DEFAULT_ROUTING);
        supportedMessages.add(DeviceMessageId.PLC_CONFIGURATION_SET_DEVICE_TYPE);

        // PLC configuration - Miscellaneous
        supportedMessages.add(DeviceMessageId.PLC_CONFIGURATION_RESET_PLC_OFDM_MAC_COUNTERS);
        supportedMessages.add(DeviceMessageId.PLC_CONFIGURATION_WRITE_PLC_G3_TIMEOUT);
        supportedMessages.add(DeviceMessageId.PLC_CONFIGURATION_WRITE_G3_KEEP_ALIVE);
    }
}