package com.energyict.protocolimplv2.dlms.idis.am540.messages;

import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.am130.messages.AM130Messaging;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.IDISMessageExecutor;
import com.energyict.protocolimplv2.messages.PLCConfigurationDeviceMessage;

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
    }
}