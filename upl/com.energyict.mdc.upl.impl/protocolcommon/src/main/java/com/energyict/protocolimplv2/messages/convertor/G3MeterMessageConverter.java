package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cbo.Password;
import com.energyict.cbo.TimeDuration;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.core.UserFile;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.*;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ActivityCalendarMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.SpecialDaysMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.FirmwareUdateWithUserFileMessageEntry;
import com.energyict.protocolimplv2.messages.enums.DlmsAuthenticationLevelMessageValues;
import com.energyict.protocolimplv2.messages.enums.DlmsEncryptionLevelMessageValues;
import com.energyict.protocolimplv2.messages.enums.LoadProfileMode;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;

/**
 * Represents a MessageConverter that maps the new G3 meter (AS330D / Sagemcom) messages to legacy XML
 *
 * @author khe
 * @since 24/10/13 - 9:38
 */
public class G3MeterMessageConverter extends AbstractMessageConverter {

    /**
     * Represents a mapping between {@link com.energyict.mdc.messages.DeviceMessageSpec deviceMessageSpecs}
     * and the corresponding {@link MessageEntryCreator}
     */
    private static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>();

    static {
        registry.put(ContactorDeviceMessage.CONTACTOR_ARM, new SimpleTagMessageEntry("ArmMainContactor"));
        registry.put(ContactorDeviceMessage.CONTACTOR_CLOSE, new SimpleTagMessageEntry("CloseMainContactor"));
        registry.put(ContactorDeviceMessage.CONTACTOR_OPEN, new SimpleTagMessageEntry("OpenMainContactor"));

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
        registry.put(PLCConfigurationDeviceMessage.PathRequest, new MultipleAttributeMessageEntry("PathRequest", "groupId"));
        registry.put(PLCConfigurationDeviceMessage.WritePlcG3Timeout, new MultipleAttributeMessageEntry("WritePlcG3Timeout", "Timeout_in_minutes"));

        registry.put(LogBookDeviceMessage.ResetMainLogbook, new SimpleTagMessageEntry("ResetMainLogbook"));
        registry.put(LogBookDeviceMessage.ResetCoverLogbook, new SimpleTagMessageEntry("ResetCoverLogbook"));
        registry.put(LogBookDeviceMessage.ResetBreakerLogbook, new SimpleTagMessageEntry("ResetBreakerLogbook"));
        registry.put(LogBookDeviceMessage.ResetCommunicationLogbook, new SimpleTagMessageEntry("ResetCommunicationLogbook"));
        registry.put(LogBookDeviceMessage.ResetVoltageCutLogbook, new SimpleTagMessageEntry("ResetVoltageCutLogbook"));
        registry.put(LogBookDeviceMessage.ResetLQILogbook, new SimpleTagMessageEntry("ResetLQILogbook"));

        registry.put(LoadProfileMessage.ResetActiveImportLP, new SimpleTagMessageEntry("ResetActiveImportLP"));
        registry.put(LoadProfileMessage.ResetActiveExportLP, new SimpleTagMessageEntry("ResetActiveExportLP"));
        registry.put(LoadProfileMessage.ResetDailyProfile, new SimpleTagMessageEntry("ResetDailyProfile"));
        registry.put(LoadProfileMessage.ResetMonthlyProfile, new SimpleTagMessageEntry("ResetMonthlyProfile"));

        registry.put(LoadProfileMessage.WRITE_CAPTURE_PERIOD_LP1, new MultipleAttributeMessageEntry("WriteProfileInterval", "IntervalInSeconds"));
        registry.put(LoadProfileMessage.WriteConsumerProducerMode, new MultipleAttributeMessageEntry("WriteConsumerProducerMode", "Mode"));

        registry.put(AdvancedTestMessage.LogObjectList, new SimpleTagMessageEntry("LogObjectList"));
        registry.put(ClockDeviceMessage.SyncTime, new SimpleTagMessageEntry("ForceSyncClock"));
        registry.put(ClockDeviceMessage.SET_TIME, new MultipleAttributeMessageEntry("WriteClockDateTime", "DateTime"));

        registry.put(SecurityMessage.CHANGE_DLMS_AUTHENTICATION_LEVEL, new MultipleAttributeMessageEntry("ChangeAuthenticationLevel", "Authentication_level"));
        registry.put(SecurityMessage.ACTIVATE_DLMS_ENCRYPTION, new MultipleAttributeMessageEntry(RtuMessageConstant.AEE_ACTIVATE_SECURITY, "Security_level"));
        registry.put(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEYS, new MultipleAttributeMessageEntry(RtuMessageConstant.NTA_AEE_CHANGE_DATATRANSPORT_AUTHENTICATION_KEY, "NewAuthenticationKey", "NewWrappedAuthenticationKey"));
        registry.put(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS, new MultipleAttributeMessageEntry(RtuMessageConstant.NTA_AEE_CHANGE_DATATRANSPORT_ENCRYPTION_KEY, "NewEncryptionKey", "NewWrappedEncryptionKey"));
        registry.put(SecurityMessage.CHANGE_HLS_SECRET_HEX, new MultipleAttributeMessageEntry(RtuMessageConstant.AEE_CHANGE_HLS_SECRET, "HLS_Secret"));
        registry.put(SecurityMessage.CHANGE_LLS_SECRET_HEX, new MultipleAttributeMessageEntry("ChangeLLSSecret", "LLS_Secret"));
        registry.put(SecurityMessage.WRITE_PSK, new MultipleAttributeMessageEntry("WritePlcPsk", "PSK"));

        registry.put(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_TYPE, new ActivityCalendarMessageEntry(activityCalendarTypeAttributeName, activityCalendarNameAttributeName, activityCalendarActivationDateAttributeName, activityCalendarCodeTableAttributeName));
        registry.put(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND_WITH_TYPE, new SpecialDaysMessageEntry(activityCalendarTypeAttributeName, specialDaysCodeTableAttributeName));

        registry.put(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_RESUME_OPTION_AND_TYPE, new FirmwareUdateWithUserFileMessageEntry(firmwareUpdateUserFileAttributeName, resumeFirmwareUpdateAttributeName, plcTypeFirmwareUpdateAttributeName));

    }

    /**
     * Default constructor for at-runtime instantiation
     */
    public G3MeterMessageConverter() {
        super();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(meterTimeAttributeName)) {
            return europeanDateTimeFormat.format((Date) messageAttribute);
        } else if (propertySpec.getName().equals(capturePeriodAttributeName)
                || propertySpec.getName().equals(broadCastLogTableEntryTTLAttributeName)) {
            return String.valueOf(((TimeDuration) messageAttribute).getSeconds());
        } else if (propertySpec.getName().equals(consumerProducerModeAttributeName)) {
            return String.valueOf(LoadProfileMode.fromDescription(messageAttribute.toString()));
        } else if (propertySpec.getName().equals(encryptionLevelAttributeName)) {
            return String.valueOf(DlmsEncryptionLevelMessageValues.getValueFor(messageAttribute.toString()));
        } else if (propertySpec.getName().equals(authenticationLevelAttributeName)) {
            return String.valueOf(DlmsAuthenticationLevelMessageValues.getValueFor(messageAttribute.toString()));
        } else if (propertySpec.getName().equals(pskAttributeName) || propertySpec.getName().equals(newHexPasswordAttributeName)) {
            return ((Password) messageAttribute).getValue();
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
        return messageAttribute.toString();
    }

    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return registry;
    }
}