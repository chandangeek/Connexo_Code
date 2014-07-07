package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.common.HexString;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.codetables.Code;
import com.energyict.mdc.protocol.api.UserFile;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.AdvancedTestMessage;
import com.energyict.protocolimplv2.messages.ClockDeviceMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadProfileMessage;
import com.energyict.protocolimplv2.messages.LogBookDeviceMessage;
import com.energyict.protocolimplv2.messages.PLCConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
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

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.MaxOrphanTimerAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activeScanDurationAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarCodeTableAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarNameAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarTypeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.authenticationLevelAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.broadCastLogTableEntryTTLAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.capturePeriodAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.consumerProducerModeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.discoveryAttemptsSpeedAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.encryptionLevelAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateUserFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.maxAgeTimeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.meterTimeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newHexPasswordAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.panConflictWaitTimeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.plcG3TimeoutAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.plcTypeFirmwareUpdateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.pskAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.resumeFirmwareUpdateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.specialDaysCodeTableAttributeName;

/**
 * Represents a MessageConverter that maps the new G3 meter (AS330D / Sagemcom) messages to legacy XML
 *
 * @author khe
 * @since 24/10/13 - 9:38
 */
public class G3MeterMessageConverter extends AbstractMessageConverter {

    /**
     * Represents a mapping between {@link DeviceMessageSpec}s
     * and the corresponding {@link MessageEntryCreator}
     */
    private static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>();

    static {
        registry.put(ContactorDeviceMessage.CONTACTOR_ARM, new SimpleTagMessageEntry("ArmMainContactor"));
        registry.put(ContactorDeviceMessage.CONTACTOR_CLOSE, new SimpleTagMessageEntry("CloseMainContactor"));
        registry.put(ContactorDeviceMessage.CONTACTOR_OPEN, new SimpleTagMessageEntry("OpenMainContactor"));

        registry.put(PLCConfigurationDeviceMessage.SetActiveScanDurationAttributeName, new MultipleAttributeMessageEntry("SetActiveScanDuration", "ActiveScanDuration"));
        registry.put(PLCConfigurationDeviceMessage.SetBroadCastLogTableEntryTTLAttributeName, new MultipleAttributeMessageEntry("SetBroadcastLogTableEntryTTL", "BroadcastLogTableEntryTTL"));
        registry.put(PLCConfigurationDeviceMessage.SetDiscoveryAttemptsSpeedAttributeName, new MultipleAttributeMessageEntry("SetDiscoveryAttemptsSpeed", "DiscoveryAttemptsSpeed"));
        registry.put(PLCConfigurationDeviceMessage.SetMaxAgeTimeAttributeName, new MultipleAttributeMessageEntry("SetMaxAgeTime", "MaxAgeTime"));
        registry.put(PLCConfigurationDeviceMessage.SetMaxNumberOfHopsAttributeName, new MultipleAttributeMessageEntry("SetMaxHops", "MaxHops"));
        registry.put(PLCConfigurationDeviceMessage.SetMaxPANConflictsCountAttributeName, new MultipleAttributeMessageEntry("SetMaxPanConflictCount", "MaxPanConflictCount"));
        registry.put(PLCConfigurationDeviceMessage.SetPanConflictWaitTimeAttributeName, new MultipleAttributeMessageEntry("SetPanConflictWaitTime", "PanConflictWaitTime"));
        registry.put(PLCConfigurationDeviceMessage.SetToneMaskAttributeName, new MultipleAttributeMessageEntry("SetToneMask", "ToneMask"));
        registry.put(PLCConfigurationDeviceMessage.SetWeakLQIValueAttributeName, new MultipleAttributeMessageEntry("SetWeakLQIValue", "WeakLQIValue"));
        registry.put(PLCConfigurationDeviceMessage.WritePlcG3Timeout, new MultipleAttributeMessageEntry("WritePlcG3Timeout", "Timeout_in_minutes"));
        registry.put(PLCConfigurationDeviceMessage.ResetPlcOfdmMacCounters, new SimpleTagMessageEntry("ResetPlcOfdmMacCounters"));
        registry.put(PLCConfigurationDeviceMessage.SetPanId, new MultipleAttributeMessageEntry("SetPanId", "panId"));
        registry.put(PLCConfigurationDeviceMessage.SetMaxOrphanTimer, new MultipleAttributeMessageEntry("SetMaxOrphanTimer", "maxOrphanTimer"));

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
        registry.put(ClockDeviceMessage.SET_TIME, new MultipleAttributeMessageEntry("WriteClockDateTime", "DateTime"));

        registry.put(SecurityMessage.CHANGE_DLMS_AUTHENTICATION_LEVEL, new MultipleAttributeMessageEntry("ChangeAuthenticationLevel", "Authentication_level"));
        registry.put(SecurityMessage.ACTIVATE_DLMS_ENCRYPTION, new MultipleAttributeMessageEntry("ActivateSecurityLevel", "Security_level"));
        registry.put(SecurityMessage.CHANGE_AUTHENTICATION_KEY, new SimpleTagMessageEntry("ChangeAuthenticationKey"));
        registry.put(SecurityMessage.CHANGE_ENCRYPTION_KEY, new SimpleTagMessageEntry("ChangeEncryptionKey"));
        registry.put(SecurityMessage.CHANGE_HLS_SECRET_HEX, new MultipleAttributeMessageEntry("ChangeHLSSecret", "HLS_Secret"));
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
        } else if (propertySpec.getName().equals(activeScanDurationAttributeName)
                || propertySpec.getName().equals(discoveryAttemptsSpeedAttributeName)
                || propertySpec.getName().equals(maxAgeTimeAttributeName)
                || propertySpec.getName().equals(MaxOrphanTimerAttributeName)
                || propertySpec.getName().equals(capturePeriodAttributeName)
                || propertySpec.getName().equals(panConflictWaitTimeAttributeName)
                || propertySpec.getName().equals(broadCastLogTableEntryTTLAttributeName)) {
            return String.valueOf(((TimeDuration) messageAttribute).getSeconds());
        } else if (propertySpec.getName().equals(plcG3TimeoutAttributeName)) {
            return String.valueOf(((TimeDuration) messageAttribute).getSeconds() / 60);  //Minutes
        } else if (propertySpec.getName().equals(consumerProducerModeAttributeName)) {
            return String.valueOf(LoadProfileMode.fromDescription(messageAttribute.toString()));
        } else if (propertySpec.getName().equals(encryptionLevelAttributeName)) {
            return String.valueOf(DlmsEncryptionLevelMessageValues.getValueFor(messageAttribute.toString()));
        } else if (propertySpec.getName().equals(authenticationLevelAttributeName)) {
            return String.valueOf(DlmsAuthenticationLevelMessageValues.getValueFor(messageAttribute.toString()));
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
            return messageAttribute.toString();
        }
        return messageAttribute.toString();
    }

    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return registry;
    }
}