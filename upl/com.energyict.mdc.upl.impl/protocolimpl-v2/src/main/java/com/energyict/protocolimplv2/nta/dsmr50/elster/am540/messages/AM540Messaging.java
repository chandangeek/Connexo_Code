package com.energyict.protocolimplv2.nta.dsmr50.elster.am540.messages;

import com.energyict.cbo.TimeDuration;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdc.protocol.tasks.support.DeviceMessageSupport;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocolimplv2.messages.PLCConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;
import com.energyict.protocolimplv2.nta.dsmr40.messages.Dsmr40Messaging;

import java.util.ArrayList;
import java.util.List;

/**
 * Messaging class for the AM540 device, which contains a combination of G3 PLC messages and DSMR 4.0 messages
 *
 * @author sva
 * @since 6/01/2015 - 9:39
 */
public class AM540Messaging extends Dsmr40Messaging implements DeviceMessageSupport {

    private final static List<DeviceMessageSpec> supportedPLCMessages;

    private AM540MessageExecutor messageExecutor;

    static {
        supportedPLCMessages = new ArrayList<>();

        // PLC configuration - G3 PLC OFDM MAC setup
        supportedPLCMessages.add(PLCConfigurationDeviceMessage.SetTMRTTL);
        supportedPLCMessages.add(PLCConfigurationDeviceMessage.SetMaxFrameRetries);
        supportedPLCMessages.add(PLCConfigurationDeviceMessage.SetNeighbourTableEntryTTL);
        supportedPLCMessages.add(PLCConfigurationDeviceMessage.SetHighPriorityWindowSize);
        supportedPLCMessages.add(PLCConfigurationDeviceMessage.SetCSMAFairnessLimit);
        supportedPLCMessages.add(PLCConfigurationDeviceMessage.SetBeaconRandomizationWindowLength);
        supportedPLCMessages.add(PLCConfigurationDeviceMessage.SetMacA);
        supportedPLCMessages.add(PLCConfigurationDeviceMessage.SetMacK);
        supportedPLCMessages.add(PLCConfigurationDeviceMessage.SetMinimumCWAttempts);
        supportedPLCMessages.add(PLCConfigurationDeviceMessage.SetMaxBe);
        supportedPLCMessages.add(PLCConfigurationDeviceMessage.SetMaxCSMABackOff);
        supportedPLCMessages.add(PLCConfigurationDeviceMessage.SetMinBe);

        // PLC configuration - G3 6LoWPAN layer setup
        supportedPLCMessages.add(PLCConfigurationDeviceMessage.SetMaxNumberOfHopsAttributeName);
        supportedPLCMessages.add(PLCConfigurationDeviceMessage.SetWeakLQIValueAttributeName);
        supportedPLCMessages.add(PLCConfigurationDeviceMessage.SetSecurityLevel);
        supportedPLCMessages.add(PLCConfigurationDeviceMessage.SetRoutingConfiguration);
        supportedPLCMessages.add(PLCConfigurationDeviceMessage.SetBroadCastLogTableEntryTTLAttributeName);
        supportedPLCMessages.add(PLCConfigurationDeviceMessage.SetMaxJoinWaitTime);
        supportedPLCMessages.add(PLCConfigurationDeviceMessage.SetPathDiscoveryTime);
        supportedPLCMessages.add(PLCConfigurationDeviceMessage.SetMetricType);
        supportedPLCMessages.add(PLCConfigurationDeviceMessage.SetCoordShortAddress);
        supportedPLCMessages.add(PLCConfigurationDeviceMessage.SetDisableDefaultRouting);
        supportedPLCMessages.add(PLCConfigurationDeviceMessage.SetDeviceType);

        // PLC configuration - Miscellaneous
        supportedPLCMessages.add(PLCConfigurationDeviceMessage.WritePlcG3Timeout);
    }

    private Dsmr40Messaging dsmr40Messaging;

    public AM540Messaging(AbstractMessageExecutor messageExecutor) {
        super(messageExecutor);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        List<DeviceMessageSpec> allSupportedMessages = new ArrayList<>();
        allSupportedMessages.addAll(supportedPLCMessages);
        allSupportedMessages.addAll(getDsmr40Messaging().getSupportedMessages());

        // DSMR5.0 security related messages who have changed compared to DSM4.0
        allSupportedMessages.remove(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY);
        allSupportedMessages.remove(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY);
        allSupportedMessages.add(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS);
        allSupportedMessages.add(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEYS);

        return allSupportedMessages;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (messageAttribute instanceof TimeDuration) {
            return Integer.toString(((TimeDuration) messageAttribute).getSeconds());
        } else {
            return super.format(propertySpec, messageAttribute);
        }
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> offlineDeviceMessages) {
        return getMessageExecutor().executePendingMessages(offlineDeviceMessages);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> offlineDeviceMessages) {
        return getMessageExecutor().updateSentMessages(offlineDeviceMessages);
    }

    /**
     * Re-uses all the DSMR 4.0 messages, except for the ones related to GPRS, MBus, reset and limiter
     */
    protected Dsmr40Messaging getDsmr40Messaging() {
        if (dsmr40Messaging == null) {
            dsmr40Messaging = new Dsmr40Messaging(getMessageExecutor());
            dsmr40Messaging.setSupportMBus(false);
            dsmr40Messaging.setSupportGPRS(false);
            dsmr40Messaging.setSupportMeterReset(false);
            dsmr40Messaging.setSupportLimiter(false);
            dsmr40Messaging.setSupportResetWindow(false);
        }
        return dsmr40Messaging;
    }
}