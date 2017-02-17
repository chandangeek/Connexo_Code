package com.energyict.protocolimplv2.dlms.idis.am540.messages;

import com.energyict.mdc.messages.DeviceMessageSpec;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.am130.messages.AM130Messaging;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.IDISMessageExecutor;
import com.energyict.protocolimplv2.messages.AlarmConfigurationMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadBalanceDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadProfileMessage;
import com.energyict.protocolimplv2.messages.LogBookDeviceMessage;
import com.energyict.protocolimplv2.messages.PLCConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;

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
    }

    private void addAdditionalDeviceMessages(List<DeviceMessageSpec> supportedMessages) {
        supportedMessages.add(FirmwareDeviceMessage.VerifyAndActivateFirmware);
        supportedMessages.add(FirmwareDeviceMessage.ENABLE_IMAGE_TRANSFER);
        supportedMessages.add(LoadBalanceDeviceMessage.UPDATE_SUPERVISION_MONITOR);
        supportedMessages.add(LoadProfileMessage.LOAD_PROFILE_OPT_IN_OUT);
        supportedMessages.add(LoadProfileMessage.SET_DISPLAY_ON_OFF);
        supportedMessages.add(LoadProfileMessage.WRITE_MEASUREMENT_PERIOD_3_FOR_INSTANTANEOUS_VALUES);
        supportedMessages.add(LogBookDeviceMessage.ResetSecurityGroupEventCounterObjects);
        supportedMessages.add(LogBookDeviceMessage.ResetAllSecurityGroupEventCounters);
        supportedMessages.add(LoadBalanceDeviceMessage.CONFIGURE_LOAD_LIMIT_PARAMETERS_EXCEPT_EMERGENCY_ONES);
        supportedMessages.add(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEYS_FOR_PREDEFINED_CLIENT);
        supportedMessages.add(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS_FOR_PREDEFINED_CLIENT);
        supportedMessages.add(SecurityMessage.CHANGE_MASTER_KEY_WITH_NEW_KEYS_FOR_PREDEFINED_CLIENT);
        supportedMessages.add(DeviceActionMessage.BILLING_RESET);
        supportedMessages.add(LoadBalanceDeviceMessage.CONFIGURE_LOAD_LIMIT_PARAMETERS_ATTRIBUTES_4TO9);
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
        supportedMessages.add(PLCConfigurationDeviceMessage.SetBroadCastLogTableEntryTTLVersion1);
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

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(DeviceMessageConstants.monitoredValueAttributeName)) {
            return messageAttribute.toString(); // Simply return as string (in IDISMessaging#format this attribute is parsed as MonitoredValue.fromDescription, which we don't want here)
        }
        return super.format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
    }
}