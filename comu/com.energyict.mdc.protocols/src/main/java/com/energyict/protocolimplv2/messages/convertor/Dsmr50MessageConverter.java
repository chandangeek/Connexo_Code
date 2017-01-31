/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.messages.convertor;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.HexString;
import com.energyict.mdc.common.Password;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.protocol.api.device.messages.DlmsEncryptionLevelMessageValues;
import com.energyict.mdc.protocol.api.device.messages.LoadProfileMode;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagMessageEntry;

import javax.inject.Inject;
import java.util.Date;
import java.util.Map;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.activityCalendarActivationDateAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.activityCalendarAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.adp_Blacklist_table_entry_TTL;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.adp_unicast_RREQ_gen_enable;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.broadCastLogTableEntryTTLAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.capturePeriodAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.consumerProducerModeAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.disableDefaultRouting;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.encryptionLevelAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.firmwareUpdateFileAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.meterTimeAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.newAuthenticationKeyAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.newEncryptionKeyAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.newHexPasswordAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.newWrappedAuthenticationKeyAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.newWrappedEncryptionKeyAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.plcG3TimeoutAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.plcTypeFirmwareUpdateAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.pskAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.resumeFirmwareUpdateAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.specialDaysAttributeName;

/**
 * Represents a MessageConverter for the legacy AM540 & Sagemcom DSMR5.0 protocols
 *
 * @author sva
 * @since 30/10/13 - 14:00
 */
public class Dsmr50MessageConverter extends Dsmr40MessageConverter {

    /**
     * Default constructor for at-runtime instantiation
     */
    @Inject
    public Dsmr50MessageConverter(TopologyService topologyService) {
        super(topologyService);
    }

    @Override
    protected void initializeRegistry(Map<DeviceMessageId, MessageEntryCreator> registry) {
        super.initializeRegistry(registry);

        //G3 PLC objects
        registry.put(DeviceMessageId.PLC_CONFIGURATION_WRITE_PLC_G3_TIMEOUT, new MultipleAttributeMessageEntry("WritePlcG3Timeout", "Timeout_in_minutes"));
        registry.put(DeviceMessageId.SECURITY_WRITE_PSK, new MultipleAttributeMessageEntry("WritePlcPsk", "PSK"));

        registry.put(DeviceMessageId.PLC_CONFIGURATION_RESET_PLC_OFDM_MAC_COUNTERS, new SimpleTagMessageEntry("ResetPlcOfdmMacCounters"));

        registry.put(DeviceMessageId.PLC_CONFIGURATION_SET_TONE_MASK_ATTRIBUTE_NAME, new MultipleAttributeMessageEntry("SetToneMask", "ToneMask"));
        registry.put(DeviceMessageId.PLC_CONFIGURATION_SET_TMR_TTL, new MultipleAttributeMessageEntry("SetTMRTTL", "tmrTTL"));
        registry.put(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_FRAME_RETRIES, new MultipleAttributeMessageEntry("SetMaxFrameRetries", "maxFrameRetries"));
        registry.put(DeviceMessageId.PLC_CONFIGURATION_SET_NEIGHBOUR_TABLE_ENTRY_TTL, new MultipleAttributeMessageEntry("SetNeighbourTableEntryTTL", "NeighbourTableEntryTTL"));
        registry.put(DeviceMessageId.PLC_CONFIGURATION_SET_HIGH_PRIORITY_WINDOW_SIZE, new MultipleAttributeMessageEntry("SetHighPriorityWindowSize", "windowSize"));
        registry.put(DeviceMessageId.PLC_CONFIGURATION_SET_CSMA_FAIRNESS_LIMIT, new MultipleAttributeMessageEntry("SetCSMAFairnessLimit", "CSMAFairnessLimit"));
        registry.put(DeviceMessageId.PLC_CONFIGURATION_SET_BEACON_RANDOMIZATION_WINDOW_LENGTH, new MultipleAttributeMessageEntry("SetBeaconRandomizationWindowLength", "WindowLength"));
        registry.put(DeviceMessageId.PLC_CONFIGURATION_SET_MAC_A, new MultipleAttributeMessageEntry("SetMacA", "MAC_A"));
        registry.put(DeviceMessageId.PLC_CONFIGURATION_SET_MAC_K, new MultipleAttributeMessageEntry("SetMacK", "MAC_K"));
        registry.put(DeviceMessageId.PLC_CONFIGURATION_SET_MINIMUM_CW_ATTEMPTS, new MultipleAttributeMessageEntry("SetMinimumCWAttempts", "minimumCWAttempts"));
        registry.put(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_BE, new MultipleAttributeMessageEntry("SetMaxBe", "maxBE"));
        registry.put(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_CSMA_BACK_OFF, new MultipleAttributeMessageEntry("SetMaxCSMABackOff", "maxCSMABackOff"));
        registry.put(DeviceMessageId.PLC_CONFIGURATION_SET_MIN_BE, new MultipleAttributeMessageEntry("SetMinBe", "minBE"));

        registry.put(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_NUMBER_OF_HOPS_ATTRIBUTENAME, new MultipleAttributeMessageEntry("SetMaxHops", "MaxHops"));
        registry.put(DeviceMessageId.PLC_CONFIGURATION_SET_WEAK_LQI_VALUE_ATTRIBUTENAME, new MultipleAttributeMessageEntry("SetWeakLQIValue", "WeakLQIValue"));
        registry.put(DeviceMessageId.PLC_CONFIGURATION_SET_SECURITY_LEVEL, new MultipleAttributeMessageEntry("SetSecurityLevel", "SecurityLevel"));
        registry.put(DeviceMessageId.PLC_CONFIGURATION_SET_ROUTING_CONFIGURATION, new MultipleAttributeMessageEntry("SetRoutingConfiguration", "adp_Kr", "adp_Km", "adp_Kc", "adp_Kq", "adp_Kh", "adp_Krt", "adp_RREQ_retries", "adp_RLC_enabled", "adp_net_traversal_time", "adp_routing_table_entry_TTL", "adp_RREQ_RERR_wait", "adp_Blacklist_table_entry_TTL", "adp_unicast_RREQ_gen_enable", "adp_add_rev_link_cost"));
        registry.put(DeviceMessageId.PLC_CONFIGURATION_SET_BROADCAST_LOG_TABLE_ENTRY_TTL_ATTRIBUTENAME, new MultipleAttributeMessageEntry("SetBroadcastLogTableEntryTTL", "BroadcastLogTableEntryTTL"));
        registry.put(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_JOIN_WAIT_TIME, new MultipleAttributeMessageEntry("SetMaxJoinWaitTime", "MaxJoinWaitTime"));
        registry.put(DeviceMessageId.PLC_CONFIGURATION_SET_PATH_DISCOVERY_TIME, new MultipleAttributeMessageEntry("SetPathDiscoveryTime", "PathDiscoveryTime"));
        registry.put(DeviceMessageId.PLC_CONFIGURATION_SET_METRIC_TYPE, new MultipleAttributeMessageEntry("SetMetricType", "MetricType"));
        registry.put(DeviceMessageId.PLC_CONFIGURATION_SET_COORD_SHORT_ADDRESS, new MultipleAttributeMessageEntry("SetCoordShortAddress", "CoordShortAddress"));
        registry.put(DeviceMessageId.PLC_CONFIGURATION_SET_DISABLE_DEFAULT_ROUTING, new MultipleAttributeMessageEntry("SetDisableDefaultRouting", "DisableDefaultRouting"));
        registry.put(DeviceMessageId.PLC_CONFIGURATION_SET_DEVICE_TYPE, new MultipleAttributeMessageEntry("SetDeviceType", "DeviceType"));

        //No longer supported in DSMR5.0
        registry.remove(DeviceMessageId.MBUS_SETUP_COMMISSION_WITH_CHANNEL);
        registry.remove(DeviceMessageId.CONFIGURATION_CHANGE_ENABLE_DISCOVERY_ON_POWER_UP);
        registry.remove(DeviceMessageId.CONFIGURATION_CHANGE_DISABLE_DISCOVERY_ON_POWER_UP);
        registry.remove(DeviceMessageId.LOAD_BALANCING_CONFIGURE_LOAD_LIMIT_PARAMETERS);
        registry.remove(DeviceMessageId.LOAD_BALANCING_SET_EMERGENCY_PROFILE_GROUP_IDS);
        registry.remove(DeviceMessageId.LOAD_BALANCING_CLEAR_LOAD_LIMIT_CONFIGURATION);
        registry.remove(DeviceMessageId.DEVICE_ACTIONS_GLOBAL_METER_RESET);
        registry.remove(DeviceMessageId.DEVICE_ACTIONS_RESTORE_FACTORY_SETTINGS);
        registry.remove(DeviceMessageId.NETWORK_CONNECTIVITY_ACTIVATE_WAKEUP_MECHANISM);
        registry.remove(DeviceMessageId.NETWORK_CONNECTIVITY_DEACTIVATE_SMS_WAKEUP);
        registry.remove(DeviceMessageId.NETWORK_CONNECTIVITY_CHANGE_GPRS_USER_CREDENTIALS);
        registry.remove(DeviceMessageId.NETWORK_CONNECTIVITY_CHANGE_GPRS_APN_CREDENTIALS);
        registry.remove(DeviceMessageId.NETWORK_CONNECTIVITY_ADD_PHONENUMBERS_TO_WHITE_LIST);
        registry.remove(DeviceMessageId.CONFIGURATION_CHANGE_CHANGE_DEFAULT_RESET_WINDOW);
        registry.remove(DeviceMessageId.ADVANCED_TEST_XML_CONFIG);
        registry.remove(DeviceMessageId.CONTACTOR_OPEN);
        registry.remove(DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE);
        registry.remove(DeviceMessageId.CONTACTOR_CLOSE);
        registry.remove(DeviceMessageId.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE);
        registry.remove(DeviceMessageId.CONTACTOR_CHANGE_CONNECT_CONTROL_MODE);

        //Messages to change the keys has changed (takes plain and wrapped key)
        registry.remove(DeviceMessageId.SECURITY_CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY);
        registry.remove(DeviceMessageId.SECURITY_CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY);
        registry.put(DeviceMessageId.SECURITY_CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEYS, new MultipleAttributeMessageEntry(RtuMessageConstant.NTA_AEE_CHANGE_DATATRANSPORT_AUTHENTICATION_KEY, RtuMessageConstant.AEE_PLAIN_NEW_AUTHENTICATION_KEY, RtuMessageConstant.AEE_NEW_AUTHENTICATION_KEY));
        registry.put(DeviceMessageId.SECURITY_CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS, new MultipleAttributeMessageEntry(RtuMessageConstant.NTA_AEE_CHANGE_DATATRANSPORT_ENCRYPTION_KEY, RtuMessageConstant.AEE_PLAIN_NEW_ENCRYPTION_KEY, RtuMessageConstant.AEE_NEW_ENCRYPTION_KEY));
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {

        //All G3 attributes are covered here (PLC and security)
        if (propertySpec.getName().equals(meterTimeAttributeName)) {
            return europeanDateTimeFormat.format((Date) messageAttribute);
        } else if (propertySpec.getName().equals(capturePeriodAttributeName)
                || propertySpec.getName().equals(broadCastLogTableEntryTTLAttributeName)) {
            return String.valueOf(((TimeDuration) messageAttribute).getSeconds());
        } else if (propertySpec.getName().equals(plcG3TimeoutAttributeName)) {
            return String.valueOf(((TimeDuration) messageAttribute).getSeconds() / 60);  //Minutes
        } else if (propertySpec.getName().equals(consumerProducerModeAttributeName)) {
            return String.valueOf(LoadProfileMode.fromDescription(messageAttribute.toString()));
        } else if (propertySpec.getName().equals(encryptionLevelAttributeName)) {
            return String.valueOf(DlmsEncryptionLevelMessageValues.getValueFor(messageAttribute.toString()));
        } else if (propertySpec.getName().equals(pskAttributeName) || propertySpec.getName().equals(newHexPasswordAttributeName)) {
            return ((HexString) messageAttribute).getContent();
        } else if (propertySpec.getName().equals(activityCalendarActivationDateAttributeName)) {
            return String.valueOf(((Date) messageAttribute).getTime());
        } else if (propertySpec.getName().equals(activityCalendarAttributeName)) {
            return convertCodeTableToXML((Calendar) messageAttribute);
        } else if (propertySpec.getName().equals(specialDaysAttributeName)) {
            return convertSpecialDaysCodeTableToXML((Calendar) messageAttribute);
        } else if (propertySpec.getName().equals(firmwareUpdateFileAttributeName)) {
            return messageAttribute.toString();     //This is the path of the temp file representing the FirmwareVersion
        } else if (propertySpec.getName().equals(resumeFirmwareUpdateAttributeName)
                || propertySpec.getName().equals(plcTypeFirmwareUpdateAttributeName)) {
            return ((Boolean) messageAttribute).toString();
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
