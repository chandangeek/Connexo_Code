package com.energyict.protocolimplv2.nta.dsmr50.elster.am540.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;

import com.energyict.protocolimpl.properties.Temporals;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.PLCConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;
import com.energyict.protocolimplv2.nta.dsmr40.messages.Dsmr40Messaging;

import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.List;

/**
 * Messaging class for the AM540 device, which contains a combination of G3 PLC messages and DSMR 4.0 messages
 *
 * @author sva
 * @since 6/01/2015 - 9:39
 */
public class AM540Messaging extends Dsmr40Messaging implements DeviceMessageSupport {

    private Dsmr40Messaging dsmr40Messaging;

    public AM540Messaging(AbstractMessageExecutor messageExecutor, Extractor extractor, PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        super(messageExecutor, extractor, propertySpecService, nlsService, converter);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        List<DeviceMessageSpec> allSupportedMessages = new ArrayList<>();
        allSupportedMessages.add(this.get(PLCConfigurationDeviceMessage.SetTMRTTL));
        allSupportedMessages.add(this.get(PLCConfigurationDeviceMessage.SetMaxFrameRetries));
        allSupportedMessages.add(this.get(PLCConfigurationDeviceMessage.SetNeighbourTableEntryTTL));
        allSupportedMessages.add(this.get(PLCConfigurationDeviceMessage.SetHighPriorityWindowSize));
        allSupportedMessages.add(this.get(PLCConfigurationDeviceMessage.SetCSMAFairnessLimit));
        allSupportedMessages.add(this.get(PLCConfigurationDeviceMessage.SetBeaconRandomizationWindowLength));
        allSupportedMessages.add(this.get(PLCConfigurationDeviceMessage.SetMacA));
        allSupportedMessages.add(this.get(PLCConfigurationDeviceMessage.SetMacK));
        allSupportedMessages.add(this.get(PLCConfigurationDeviceMessage.SetMinimumCWAttempts));
        allSupportedMessages.add(this.get(PLCConfigurationDeviceMessage.SetMaxBe));
        allSupportedMessages.add(this.get(PLCConfigurationDeviceMessage.SetMaxCSMABackOff));
        allSupportedMessages.add(this.get(PLCConfigurationDeviceMessage.SetMinBe));
        allSupportedMessages.add(this.get(PLCConfigurationDeviceMessage.SetMaxNumberOfHopsAttributeName));
        allSupportedMessages.add(this.get(PLCConfigurationDeviceMessage.SetWeakLQIValueAttributeName));
        allSupportedMessages.add(this.get(PLCConfigurationDeviceMessage.SetSecurityLevel));
        allSupportedMessages.add(this.get(PLCConfigurationDeviceMessage.SetRoutingConfiguration));
        allSupportedMessages.add(this.get(PLCConfigurationDeviceMessage.SetBroadCastLogTableEntryTTLAttributeName));
        allSupportedMessages.add(this.get(PLCConfigurationDeviceMessage.SetMaxJoinWaitTime));
        allSupportedMessages.add(this.get(PLCConfigurationDeviceMessage.SetPathDiscoveryTime));
        allSupportedMessages.add(this.get(PLCConfigurationDeviceMessage.SetMetricType));
        allSupportedMessages.add(this.get(PLCConfigurationDeviceMessage.SetCoordShortAddress));
        allSupportedMessages.add(this.get(PLCConfigurationDeviceMessage.SetDisableDefaultRouting));
        allSupportedMessages.add(this.get(PLCConfigurationDeviceMessage.SetDeviceType));
        allSupportedMessages.add(this.get(PLCConfigurationDeviceMessage.WritePlcG3Timeout));
        allSupportedMessages.add(this.get(ContactorDeviceMessage.CLOSE_RELAY));
        allSupportedMessages.add(this.get(ContactorDeviceMessage.OPEN_RELAY));
        allSupportedMessages.addAll(getDsmr40Messaging().getSupportedMessages());

        // DSMR5.0 security related messages who have changed compared to DSM4.0
        allSupportedMessages.remove(this.get(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY));
        allSupportedMessages.remove(this.get(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY));
        allSupportedMessages.add(this.get(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS));
        allSupportedMessages.add(this.get(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEYS));

        return allSupportedMessages;
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, com.energyict.mdc.upl.properties.PropertySpec propertySpec, Object messageAttribute) {
        if (messageAttribute instanceof TemporalAmount) {
            return Long.toString(Temporals.toSeconds((TemporalAmount) messageAttribute));
        } else {
            return super.format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
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
            dsmr40Messaging = new Dsmr40Messaging(getMessageExecutor(), this.getExtractor(), this.getPropertySpecService(), this.getNlsService(), this.getConverter());
            dsmr40Messaging.setSupportLimiter(true);
            dsmr40Messaging.setSupportMBus(false);
            dsmr40Messaging.setSupportGPRS(false);
            dsmr40Messaging.setSupportMeterReset(false);
            dsmr40Messaging.setSupportResetWindow(false);
        }
        return dsmr40Messaging;
    }
}