package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cbo.HexString;
import com.energyict.cbo.Password;
import com.energyict.cbo.TimeDuration;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.core.UserFile;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.*;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagMessageEntry;
import com.energyict.protocolimplv2.messages.enums.DlmsEncryptionLevelMessageValues;
import com.energyict.protocolimplv2.messages.enums.LoadProfileMode;

import java.util.Date;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;

/**
 * Represents a MessageConverter for the legacy AM540 & Sagemcom DSMR5.0 protocols
 *
 * @author sva
 * @since 30/10/13 - 14:00
 */
public class Dsmr50MessageConverter extends Dsmr40MessageConverter {

    static {

        //G3 PLC objects
        registry.put(PLCConfigurationDeviceMessage.WritePlcG3Timeout, new MultipleAttributeMessageEntry("WritePlcG3Timeout", "Timeout_in_minutes"));
        registry.put(SecurityMessage.WRITE_PSK, new MultipleAttributeMessageEntry("WritePlcPsk", "PSK"));

        registry.put(PLCConfigurationDeviceMessage.ResetPlcOfdmMacCounters, new SimpleTagMessageEntry("ResetPlcOfdmMacCounters"));

        registry.put(PLCConfigurationDeviceMessage.SetToneMaskAttributeName, new MultipleAttributeMessageEntry("SetToneMask", "ToneMask"));
        registry.put(PLCConfigurationDeviceMessage.SetTMRTTL, new MultipleAttributeMessageEntry("SetTMRTTL", "tmrTTL"));
        registry.put(PLCConfigurationDeviceMessage.SetMaxFrameRetries, new MultipleAttributeMessageEntry("SetMaxFrameRetries", "maxFrameRetries"));
        registry.put(PLCConfigurationDeviceMessage.SetNeighbourTableEntryTTL, new MultipleAttributeMessageEntry("SetNeighbourTableEntryTTL", "NeighbourTableEntryTTL"));
        registry.put(PLCConfigurationDeviceMessage.SetHighPriorityWindowSize, new MultipleAttributeMessageEntry("SetHighPriorityWindowSize", "windowSize"));
        registry.put(PLCConfigurationDeviceMessage.SetCSMAFairnessLimit, new MultipleAttributeMessageEntry("SetCSMAFairnessLimit", "CSMAFairnessLimit"));
        registry.put(PLCConfigurationDeviceMessage.SetBeaconRandomizationWindowLength, new MultipleAttributeMessageEntry("SetBeaconRandomizationWindowLength", "WindowLength"));
        registry.put(PLCConfigurationDeviceMessage.SetMacA, new MultipleAttributeMessageEntry("SetMacA", "MAC_A"));
        registry.put(PLCConfigurationDeviceMessage.SetMacK, new MultipleAttributeMessageEntry("SetMacK", "MAC_K"));
        registry.put(PLCConfigurationDeviceMessage.SetMinimumCWAttempts, new MultipleAttributeMessageEntry("SetMinimumCWAttempts", "minimumCWAttempts"));
        registry.put(PLCConfigurationDeviceMessage.SetMaxBe, new MultipleAttributeMessageEntry("SetMaxBe", "maxBE"));
        registry.put(PLCConfigurationDeviceMessage.SetMaxCSMABackOff, new MultipleAttributeMessageEntry("SetMaxCSMABackOff", "maxCSMABackOff"));
        registry.put(PLCConfigurationDeviceMessage.SetMinBe, new MultipleAttributeMessageEntry("SetMinBe", "minBE"));

        registry.put(PLCConfigurationDeviceMessage.SetMaxNumberOfHopsAttributeName, new MultipleAttributeMessageEntry("SetMaxHops", "MaxHops"));
        registry.put(PLCConfigurationDeviceMessage.SetWeakLQIValueAttributeName, new MultipleAttributeMessageEntry("SetWeakLQIValue", "WeakLQIValue"));
        registry.put(PLCConfigurationDeviceMessage.SetSecurityLevel, new MultipleAttributeMessageEntry("SetSecurityLevel", "SecurityLevel"));
        registry.put(PLCConfigurationDeviceMessage.SetRoutingConfiguration, new MultipleAttributeMessageEntry("SetRoutingConfiguration", "adp_Kr", "adp_Km", "adp_Kc", "adp_Kq", "adp_Kh", "adp_Krt", "adp_RREQ_retries", "adp_RLC_enabled", "adp_net_traversal_time", "adp_routing_table_entry_TTL", "adp_RREQ_RERR_wait", "adp_Blacklist_table_entry_TTL", "adp_unicast_RREQ_gen_enable", "adp_add_rev_link_cost"));
        registry.put(PLCConfigurationDeviceMessage.SetBroadCastLogTableEntryTTLAttributeName, new MultipleAttributeMessageEntry("SetBroadcastLogTableEntryTTL", "BroadcastLogTableEntryTTL"));
        registry.put(PLCConfigurationDeviceMessage.SetMaxJoinWaitTime, new MultipleAttributeMessageEntry("SetMaxJoinWaitTime", "MaxJoinWaitTime"));
        registry.put(PLCConfigurationDeviceMessage.SetPathDiscoveryTime, new MultipleAttributeMessageEntry("SetPathDiscoveryTime", "PathDiscoveryTime"));
        registry.put(PLCConfigurationDeviceMessage.SetMetricType, new MultipleAttributeMessageEntry("SetMetricType", "MetricType"));
        registry.put(PLCConfigurationDeviceMessage.SetCoordShortAddress, new MultipleAttributeMessageEntry("SetCoordShortAddress", "CoordShortAddress"));
        registry.put(PLCConfigurationDeviceMessage.SetDisableDefaultRouting, new MultipleAttributeMessageEntry("SetDisableDefaultRouting", "DisableDefaultRouting"));
        registry.put(PLCConfigurationDeviceMessage.SetDeviceType, new MultipleAttributeMessageEntry("SetDeviceType", "DeviceType"));

        //No longer supported in DSMR5.0
        registry.remove(MBusSetupDeviceMessage.Commission_With_Channel);
        registry.remove(ConfigurationChangeDeviceMessage.ENABLE_DISCOVERY_ON_POWER_UP);
        registry.remove(ConfigurationChangeDeviceMessage.DISABLE_DISCOVERY_ON_POWER_UP);
        registry.remove(LoadBalanceDeviceMessage.CONFIGURE_LOAD_LIMIT_PARAMETERS);
        registry.remove(LoadBalanceDeviceMessage.SET_EMERGENCY_PROFILE_GROUP_IDS);
        registry.remove(LoadBalanceDeviceMessage.CLEAR_LOAD_LIMIT_CONFIGURATION);
        registry.remove(DeviceActionMessage.GLOBAL_METER_RESET);
        registry.remove(DeviceActionMessage.RESTORE_FACTORY_SETTINGS);
        registry.remove(NetworkConnectivityMessage.ACTIVATE_WAKEUP_MECHANISM);
        registry.remove(NetworkConnectivityMessage.DEACTIVATE_SMS_WAKEUP);
        registry.remove(NetworkConnectivityMessage.CHANGE_GPRS_USER_CREDENTIALS);
        registry.remove(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS);
        registry.remove(NetworkConnectivityMessage.ADD_PHONENUMBERS_TO_WHITE_LIST);
        registry.remove(ConfigurationChangeDeviceMessage.ChangeDefaultResetWindow);
        registry.remove(AdvancedTestMessage.XML_CONFIG);

        //Messages to change the keys has changed (takes plain and wrapped key)
        registry.remove(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY);
        registry.remove(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY);
        registry.put(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEYS, new MultipleAttributeMessageEntry(RtuMessageConstant.NTA_AEE_CHANGE_DATATRANSPORT_AUTHENTICATION_KEY, RtuMessageConstant.AEE_PLAIN_NEW_AUTHENTICATION_KEY, RtuMessageConstant.AEE_NEW_AUTHENTICATION_KEY));
        registry.put(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS, new MultipleAttributeMessageEntry(RtuMessageConstant.NTA_AEE_CHANGE_DATATRANSPORT_ENCRYPTION_KEY, RtuMessageConstant.AEE_PLAIN_NEW_ENCRYPTION_KEY, RtuMessageConstant.AEE_NEW_ENCRYPTION_KEY));
    }

    /**
     * Default constructor for at-runtime instantiation
     */
    public Dsmr50MessageConverter() {
        super();
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
        } else if (propertySpec.getName().equals(activityCalendarCodeTableAttributeName)) {
            return convertCodeTableToXML((Code) messageAttribute);
        } else if (propertySpec.getName().equals(specialDaysCodeTableAttributeName)) {
            return convertSpecialDaysCodeTableToXML((Code) messageAttribute);
        } else if (propertySpec.getName().equals(firmwareUpdateUserFileAttributeName)) {
            UserFile userFile = (UserFile) messageAttribute;
            return new String(userFile.loadFileInByteArray());  //Bytes of the userFile, as a string
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
