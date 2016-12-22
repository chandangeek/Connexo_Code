package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.Password;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.properties.Temporals;
import com.energyict.protocolimplv2.messages.AdvancedTestMessage;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.LoadBalanceDeviceMessage;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.messages.PLCConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.enums.LoadProfileMode;

import java.time.temporal.TemporalAmount;
import java.util.HashMap;
import java.util.Map;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.adp_Blacklist_table_entry_TTL;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.adp_unicast_RREQ_gen_enable;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.broadCastLogTableEntryTTLAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.consumerProducerModeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.disableDefaultRouting;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newAuthenticationKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newEncryptionKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newWrappedAuthenticationKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newWrappedEncryptionKeyAttributeName;

/**
 * Represents a MessageConverter for the legacy AM540 & Sagemcom DSMR5.0 protocols
 *
 * @author sva
 * @since 30/10/13 - 14:00
 */
public class Dsmr50MessageConverter extends Dsmr40MessageConverter {

    public Dsmr50MessageConverter(Messaging messagingProtocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        super(messagingProtocol, propertySpecService, nlsService, converter);
    }

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>(super.getRegistry());
        //G3 PLC objects
        registry.put(messageSpec(PLCConfigurationDeviceMessage.WritePlcG3Timeout), new MultipleAttributeMessageEntry("WritePlcG3Timeout", "Timeout_in_minutes"));

        registry.put(messageSpec(PLCConfigurationDeviceMessage.SetTMRTTL), new MultipleAttributeMessageEntry("SetTMRTTL", "tmrTTL"));
        registry.put(messageSpec(PLCConfigurationDeviceMessage.SetMaxFrameRetries), new MultipleAttributeMessageEntry("SetMaxFrameRetries", "maxFrameRetries"));
        registry.put(messageSpec(PLCConfigurationDeviceMessage.SetNeighbourTableEntryTTL), new MultipleAttributeMessageEntry("SetNeighbourTableEntryTTL", "NeighbourTableEntryTTL"));
        registry.put(messageSpec(PLCConfigurationDeviceMessage.SetHighPriorityWindowSize), new MultipleAttributeMessageEntry("SetHighPriorityWindowSize", "windowSize"));
        registry.put(messageSpec(PLCConfigurationDeviceMessage.SetCSMAFairnessLimit), new MultipleAttributeMessageEntry("SetCSMAFairnessLimit", "CSMAFairnessLimit"));
        registry.put(messageSpec(PLCConfigurationDeviceMessage.SetBeaconRandomizationWindowLength), new MultipleAttributeMessageEntry("SetBeaconRandomizationWindowLength", "WindowLength"));
        registry.put(messageSpec(PLCConfigurationDeviceMessage.SetMacA), new MultipleAttributeMessageEntry("SetMacA", "MAC_A"));
        registry.put(messageSpec(PLCConfigurationDeviceMessage.SetMacK), new MultipleAttributeMessageEntry("SetMacK", "MAC_K"));
        registry.put(messageSpec(PLCConfigurationDeviceMessage.SetMinimumCWAttempts), new MultipleAttributeMessageEntry("SetMinimumCWAttempts", "minimumCWAttempts"));
        registry.put(messageSpec(PLCConfigurationDeviceMessage.SetMaxBe), new MultipleAttributeMessageEntry("SetMaxBe", "maxBE"));
        registry.put(messageSpec(PLCConfigurationDeviceMessage.SetMaxCSMABackOff), new MultipleAttributeMessageEntry("SetMaxCSMABackOff", "maxCSMABackOff"));
        registry.put(messageSpec(PLCConfigurationDeviceMessage.SetMinBe), new MultipleAttributeMessageEntry("SetMinBe", "minBE"));

        registry.put(messageSpec(PLCConfigurationDeviceMessage.SetMaxNumberOfHopsAttributeName), new MultipleAttributeMessageEntry("SetMaxHops", "MaxHops"));
        registry.put(messageSpec(PLCConfigurationDeviceMessage.SetWeakLQIValueAttributeName), new MultipleAttributeMessageEntry("SetWeakLQIValue", "WeakLQIValue"));
        registry.put(messageSpec(PLCConfigurationDeviceMessage.SetSecurityLevel), new MultipleAttributeMessageEntry("SetSecurityLevel", "SecurityLevel"));
        registry.put(messageSpec(PLCConfigurationDeviceMessage.SetRoutingConfiguration), new MultipleAttributeMessageEntry("SetRoutingConfiguration", "adp_Kr", "adp_Km", "adp_Kc", "adp_Kq", "adp_Kh", "adp_Krt", "adp_RREQ_retries", "adp_RLC_enabled", "adp_net_traversal_time", "adp_routing_table_entry_TTL", "adp_RREQ_RERR_wait", "adp_Blacklist_table_entry_TTL", "adp_unicast_RREQ_gen_enable", "adp_add_rev_link_cost"));
        registry.put(messageSpec(PLCConfigurationDeviceMessage.SetBroadCastLogTableEntryTTLAttributeName), new MultipleAttributeMessageEntry("SetBroadcastLogTableEntryTTL", "BroadcastLogTableEntryTTL"));
        registry.put(messageSpec(PLCConfigurationDeviceMessage.SetMaxJoinWaitTime), new MultipleAttributeMessageEntry("SetMaxJoinWaitTime", "MaxJoinWaitTime"));
        registry.put(messageSpec(PLCConfigurationDeviceMessage.SetPathDiscoveryTime), new MultipleAttributeMessageEntry("SetPathDiscoveryTime", "PathDiscoveryTime"));
        registry.put(messageSpec(PLCConfigurationDeviceMessage.SetMetricType), new MultipleAttributeMessageEntry("SetMetricType", "MetricType"));
        registry.put(messageSpec(PLCConfigurationDeviceMessage.SetCoordShortAddress), new MultipleAttributeMessageEntry("SetCoordShortAddress", "CoordShortAddress"));
        registry.put(messageSpec(PLCConfigurationDeviceMessage.SetDisableDefaultRouting), new MultipleAttributeMessageEntry("SetDisableDefaultRouting", "DisableDefaultRouting"));
        registry.put(messageSpec(PLCConfigurationDeviceMessage.SetDeviceType), new MultipleAttributeMessageEntry("SetDeviceType", "DeviceType"));

        //No longer supported in DSMR5.0
        registry.remove(messageSpec(MBusSetupDeviceMessage.Commission_With_Channel));
        registry.remove(messageSpec(ConfigurationChangeDeviceMessage.ENABLE_DISCOVERY_ON_POWER_UP));
        registry.remove(messageSpec(ConfigurationChangeDeviceMessage.DISABLE_DISCOVERY_ON_POWER_UP));
        registry.remove(messageSpec(LoadBalanceDeviceMessage.CONFIGURE_LOAD_LIMIT_PARAMETERS));
        registry.remove(messageSpec(LoadBalanceDeviceMessage.SET_EMERGENCY_PROFILE_GROUP_IDS));
        registry.remove(messageSpec(LoadBalanceDeviceMessage.CLEAR_LOAD_LIMIT_CONFIGURATION));
        registry.remove(messageSpec(DeviceActionMessage.GLOBAL_METER_RESET));
        registry.remove(messageSpec(DeviceActionMessage.RESTORE_FACTORY_SETTINGS));
        registry.remove(messageSpec(NetworkConnectivityMessage.ACTIVATE_WAKEUP_MECHANISM));
        registry.remove(messageSpec(NetworkConnectivityMessage.DEACTIVATE_SMS_WAKEUP));
        registry.remove(messageSpec(NetworkConnectivityMessage.CHANGE_GPRS_USER_CREDENTIALS));
        registry.remove(messageSpec(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS));
        registry.remove(messageSpec(NetworkConnectivityMessage.ADD_PHONENUMBERS_TO_WHITE_LIST));
        registry.remove(messageSpec(ConfigurationChangeDeviceMessage.ChangeDefaultResetWindow));
        registry.remove(messageSpec(AdvancedTestMessage.XML_CONFIG));
        registry.remove(messageSpec(ContactorDeviceMessage.CONTACTOR_OPEN));
        registry.remove(messageSpec(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE));
        registry.remove(messageSpec(ContactorDeviceMessage.CONTACTOR_CLOSE));
        registry.remove(messageSpec(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE));
        registry.remove(messageSpec(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE));

        //Messages to change the keys has changed (takes plain and wrapped key)
        registry.remove(messageSpec(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY));
        registry.remove(messageSpec(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY));
        registry.put(messageSpec(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEYS), new MultipleAttributeMessageEntry(RtuMessageConstant.NTA_AEE_CHANGE_DATATRANSPORT_AUTHENTICATION_KEY, RtuMessageConstant.AEE_PLAIN_NEW_AUTHENTICATION_KEY, RtuMessageConstant.AEE_NEW_AUTHENTICATION_KEY));
        registry.put(messageSpec(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS), new MultipleAttributeMessageEntry(RtuMessageConstant.NTA_AEE_CHANGE_DATATRANSPORT_ENCRYPTION_KEY, RtuMessageConstant.AEE_PLAIN_NEW_ENCRYPTION_KEY, RtuMessageConstant.AEE_NEW_ENCRYPTION_KEY));
        return registry;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {

        //All G3 attributes are covered here (PLC and security)
        if (propertySpec.getName().equals(broadCastLogTableEntryTTLAttributeName)) {
            return String.valueOf(Temporals.toSeconds((TemporalAmount) messageAttribute));
        } else if (propertySpec.getName().equals(consumerProducerModeAttributeName)) {
            return String.valueOf(LoadProfileMode.fromDescription(messageAttribute.toString()));
        } else if (propertySpec.getName().equals(newAuthenticationKeyAttributeName) ||
                propertySpec.getName().equals(newWrappedAuthenticationKeyAttributeName) ||
                propertySpec.getName().equals(newEncryptionKeyAttributeName) ||
                propertySpec.getName().equals(newWrappedEncryptionKeyAttributeName)) {
            return ((Password) messageAttribute).getValue();
        } else if (propertySpec.getName().equals(disableDefaultRouting)
                || propertySpec.getName().equals(adp_Blacklist_table_entry_TTL)
                || propertySpec.getName().equals(adp_unicast_RREQ_gen_enable)
                ) {
            return ((Boolean) messageAttribute) ? "1" : "0";
        }

        //DSMR attributes are covered in super
        return super.format(propertySpec, messageAttribute);
    }
}
