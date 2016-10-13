package com.energyict.protocolimplv2.dlms.idis.am540.messages;

import com.energyict.mdc.messages.DeviceMessageSpec;

import com.energyict.protocolimplv2.ace4000.objects.LoadProfile;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.am130.messages.AM130Messaging;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.IDISMessageExecutor;
import com.energyict.protocolimplv2.messages.AlarmConfigurationMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadBalanceDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadProfileMessage;
import com.energyict.protocolimplv2.messages.PLCConfigurationDeviceMessage;

import java.util.ArrayList;
import java.util.List;

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
            this.messageExecutor = new AM540MessageExecutor(getProtocol());
        }
        return messageExecutor;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        if (supportedMessages == null) {
            supportedMessages = new ArrayList<>();
            addSupportedDeviceMessages(supportedMessages);
        }
        return supportedMessages;
    }

    @Override
    protected void addSupportedDeviceMessages(List<DeviceMessageSpec> supportedMessages) {
        addCommonDeviceMessages(supportedMessages);
        addAlarmConfigurationMessages(supportedMessages);
        addContactorDeviceMessages(supportedMessages);
        addPLCConfigurationDeviceMessages(supportedMessages);
        addAdditionalDeviceMessages(supportedMessages);
        supportedMessages.add(LoadBalanceDeviceMessage.UPDATE_SUPERVISION_MONITOR);
        supportedMessages.add(LoadProfileMessage.LOAD_PROFILE_OPT_IN_OUT);
    }

    private void addAdditionalDeviceMessages(List<DeviceMessageSpec> supportedMessages) {
        supportedMessages.add(FirmwareDeviceMessage.VerifyAndActivateFirmware);
        supportedMessages.add(FirmwareDeviceMessage.ENABLE_IMAGE_TRANSFER);
    }

    @Override
    protected void addAlarmConfigurationMessages(List<DeviceMessageSpec> supportedMessages) {
        super.addAlarmConfigurationMessages(supportedMessages);
        supportedMessages.add(AlarmConfigurationMessage.RESET_ALL_ALARM_BITS);
    }

    private void addPLCConfigurationDeviceMessages(List<DeviceMessageSpec> supportedMessages) {
        // PLC configuration - G3-PLC MAC setup
        supportedMessages.add(PLCConfigurationDeviceMessage.SetTMRTTL);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetMaxFrameRetries);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetNeighbourTableEntryTTL);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetHighPriorityWindowSize);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetCSMAFairnessLimit);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetBeaconRandomizationWindowLength);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetMacA);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetMacK);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetMinimumCWAttempts);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetMaxBe);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetMaxCSMABackOff);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetMinBe);

        // PLC configuration - G3-PLC MAC 6LoWPAN layer setup
        supportedMessages.add(PLCConfigurationDeviceMessage.SetMaxNumberOfHopsAttributeName);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetWeakLQIValueAttributeName);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetSecurityLevel);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetRoutingConfiguration);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetBroadCastLogTableEntryTTLAttributeName);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetMaxJoinWaitTime);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetPathDiscoveryTime);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetMetricType);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetCoordShortAddress);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetDisableDefaultRouting);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetDeviceType);

        // PLC configuration - Miscellaneous
        supportedMessages.add(PLCConfigurationDeviceMessage.ResetPlcOfdmMacCounters);
        supportedMessages.add(PLCConfigurationDeviceMessage.WritePlcG3Timeout);
        supportedMessages.add(PLCConfigurationDeviceMessage.ConfigurePLcG3KeepAlive);
    }
}