package com.energyict.protocolimplv2.dlms.idis.am540.messages;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

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

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author sva
 * @since 11/08/2015 - 16:14
 */
public class AM540Messaging extends AM130Messaging {

    public AM540Messaging(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(protocol, collectedDataFactory, issueFactory, propertySpecService, nlsService, converter, calendarExtractor, messageFileExtractor, keyAccessorTypeExtractor);
    }

    protected IDISMessageExecutor getMessageExecutor() {
        if (messageExecutor == null) {
            this.messageExecutor = new AM540MessageExecutor(getProtocol(), this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return messageExecutor;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return addSupportedDeviceMessages(new ArrayList<>());
    }

    @Override
    protected List<DeviceMessageSpec> addSupportedDeviceMessages(List<DeviceMessageSpec> supportedMessages) {
        addCommonDeviceMessages(supportedMessages);
        addAlarmConfigurationMessages(supportedMessages);
        addContactorDeviceMessages(supportedMessages);
        addPLCConfigurationDeviceMessages(supportedMessages);
        addAdditionalDeviceMessages(supportedMessages);
        return supportedMessages;
    }

    private void addAdditionalDeviceMessages(List<DeviceMessageSpec> supportedMessages) {
        supportedMessages.add(FirmwareDeviceMessage.ENABLE_AND_INITIATE_IMAGE_TRANSFER.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(FirmwareDeviceMessage.VerifyAndActivateFirmware.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(FirmwareDeviceMessage.VerifyAndActivateFirmwareAtGivenDate.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(FirmwareDeviceMessage.ENABLE_IMAGE_TRANSFER.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(FirmwareDeviceMessage.CONFIGURABLE_IMAGE_TRANSFER_WITH_RESUME_OPTION.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(LoadBalanceDeviceMessage.UPDATE_SUPERVISION_MONITOR.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(LoadProfileMessage.LOAD_PROFILE_OPT_IN_OUT.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(LoadProfileMessage.SET_DISPLAY_ON_OFF.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(LoadProfileMessage.WRITE_MEASUREMENT_PERIOD_3_FOR_INSTANTANEOUS_VALUES.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(LogBookDeviceMessage.ResetSecurityGroupEventCounterObjects.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(LogBookDeviceMessage.ResetAllSecurityGroupEventCounters.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(LoadBalanceDeviceMessage.CONFIGURE_LOAD_LIMIT_PARAMETERS_EXCEPT_EMERGENCY_ONES.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY_FOR_PREDEFINED_CLIENT.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY_FOR_PREDEFINED_CLIENT.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(SecurityMessage.CHANGE_MASTER_KEY_WITH_NEW_KEY_FOR_PREDEFINED_CLIENT.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(SecurityMessage.CHANGE_PSK_WITH_NEW_KEYS.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(DeviceActionMessage.BILLING_RESET.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(LoadBalanceDeviceMessage.CONFIGURE_LOAD_LIMIT_PARAMETERS_ATTRIBUTES_4TO9.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(DeviceActionMessage.BillingResetWithActivationDate.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
    }

    @Override
    protected void addAlarmConfigurationMessages(List<DeviceMessageSpec> supportedMessages) {
        super.addAlarmConfigurationMessages(supportedMessages);
        supportedMessages.add(AlarmConfigurationMessage.RESET_ALL_ALARM_BITS.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
    }

    private void addPLCConfigurationDeviceMessages(List<DeviceMessageSpec> supportedMessages) {
        // PLC configuration - G3-PLC MAC setup
        supportedMessages.add(PLCConfigurationDeviceMessage.SetTMRTTL.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(PLCConfigurationDeviceMessage.SetMaxFrameRetries.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(PLCConfigurationDeviceMessage.SetNeighbourTableEntryTTL.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(PLCConfigurationDeviceMessage.SetHighPriorityWindowSize.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(PLCConfigurationDeviceMessage.SetCSMAFairnessLimit.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(PLCConfigurationDeviceMessage.SetBeaconRandomizationWindowLength.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(PLCConfigurationDeviceMessage.SetMacA.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(PLCConfigurationDeviceMessage.SetMacK.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(PLCConfigurationDeviceMessage.SetMinimumCWAttempts.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(PLCConfigurationDeviceMessage.SetMaxBe.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(PLCConfigurationDeviceMessage.SetMaxCSMABackOff.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(PLCConfigurationDeviceMessage.SetMinBe.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));

        // PLC configuration - G3-PLC MAC 6LoWPAN layer setup

        supportedMessages.add(PLCConfigurationDeviceMessage.SetMaxNumberOfHopsAttributeName.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(PLCConfigurationDeviceMessage.SetWeakLQIValueAttributeName.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(PLCConfigurationDeviceMessage.SetSecurityLevel.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(PLCConfigurationDeviceMessage.SetRoutingConfiguration.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(PLCConfigurationDeviceMessage.SetBroadCastLogTableEntryTTLVersion1.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(PLCConfigurationDeviceMessage.SetMaxJoinWaitTime.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(PLCConfigurationDeviceMessage.SetPathDiscoveryTime.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(PLCConfigurationDeviceMessage.SetMetricType.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(PLCConfigurationDeviceMessage.SetCoordShortAddress.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(PLCConfigurationDeviceMessage.SetDisableDefaultRouting.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(PLCConfigurationDeviceMessage.SetDeviceType.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));

        // PLC configuration - Miscellaneous
        supportedMessages.add(PLCConfigurationDeviceMessage.ResetPlcOfdmMacCounters.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(PLCConfigurationDeviceMessage.WritePlcG3Timeout.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(PLCConfigurationDeviceMessage.ConfigurePLcG3KeepAlive.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(PLCConfigurationDeviceMessage.SetAdpLBPAssociationSetup_7_Parameters.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
        supportedMessages.add(PLCConfigurationDeviceMessage.SetAdpLBPAssociationSetup_5_Parameters.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()));
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(DeviceMessageConstants.monitoredValueAttributeName)) {
            return messageAttribute.toString(); // Simply return as string (in IDISMessaging#format this attribute is parsed as MonitoredValue.fromDescription, which we don't want here)
        } else if (propertySpec.getName().equals(DeviceMessageConstants.firmwareUpdateActivationDateAttributeName)) {
            return String.valueOf(((Date) messageAttribute).getTime());     //Epoch) {
        } else if (propertySpec.getName().equals(DeviceMessageConstants.pathDiscoveryTime)){
            return String.valueOf(((Duration) messageAttribute).getSeconds()); //Return value in seconds
        }
        return super.format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
    }
}