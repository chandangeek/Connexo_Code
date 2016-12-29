package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.Password;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TariffCalendar;

import com.energyict.protocolimpl.messages.RtuMessageConstant;
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
import com.google.common.collect.ImmutableMap;

import java.time.Duration;
import java.util.Date;
import java.util.Map;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarCodeTableAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarNameAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarTypeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.adp_Blacklist_table_entry_TTL;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.adp_unicast_RREQ_gen_enable;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.authenticationLevelAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.broadCastLogTableEntryTTLAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.capturePeriodAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.consumerProducerModeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.disableDefaultRouting;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.encryptionLevelAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateUserFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.meterTimeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newAuthenticationKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newEncryptionKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newHexPasswordAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newWrappedAuthenticationKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newWrappedEncryptionKeyAttributeName;
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

    public G3MeterMessageConverter(Messaging messagingProtocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, Extractor extractor) {
        super(messagingProtocol, propertySpecService, nlsService, converter, extractor);
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(meterTimeAttributeName)) {
            return europeanDateTimeFormat.format((Date) messageAttribute);
        } else if (propertySpec.getName().equals(capturePeriodAttributeName)
                || propertySpec.getName().equals(broadCastLogTableEntryTTLAttributeName)) {
            return String.valueOf(((Duration) messageAttribute).getSeconds());
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
            return convertCodeTableToXML((TariffCalendar) messageAttribute);
        } else if (propertySpec.getName().equals(specialDaysCodeTableAttributeName)) {
            return convertSpecialDaysCodeTableToXML((TariffCalendar) messageAttribute);
        } else if (propertySpec.getName().equals(firmwareUpdateUserFileAttributeName)) {
            return this.getExtractor().contents((DeviceMessageFile) messageAttribute);  //Bytes of the userFile, as a string
        } else if (propertySpec.getName().equals(resumeFirmwareUpdateAttributeName)
                || propertySpec.getName().equals(plcTypeFirmwareUpdateAttributeName)) {
            return messageAttribute.toString();
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
        return ImmutableMap
                .<DeviceMessageSpec, MessageEntryCreator>builder()
                .put(messageSpec(ContactorDeviceMessage.CONTACTOR_ARM), new SimpleTagMessageEntry("ArmMainContactor"))
                .put(messageSpec(ContactorDeviceMessage.CONTACTOR_CLOSE), new SimpleTagMessageEntry("CloseMainContactor"))
                .put(messageSpec(ContactorDeviceMessage.CONTACTOR_OPEN), new SimpleTagMessageEntry("OpenMainContactor"))

                .put(messageSpec(PLCConfigurationDeviceMessage.SetMaxNumberOfHopsAttributeName), new MultipleAttributeMessageEntry("SetMaxHops", "MaxHops"))
                .put(messageSpec(PLCConfigurationDeviceMessage.SetWeakLQIValueAttributeName), new MultipleAttributeMessageEntry("SetWeakLQIValue", "WeakLQIValue"))
                .put(messageSpec(PLCConfigurationDeviceMessage.SetSecurityLevel), new MultipleAttributeMessageEntry("SetSecurityLevel", "SecurityLevel"))
                .put(messageSpec(PLCConfigurationDeviceMessage.SetRoutingConfiguration), new MultipleAttributeMessageEntry("SetRoutingConfiguration", "adp_Kr", "adp_Km", "adp_Kc", "adp_Kq", "adp_Kh", "adp_Krt", "adp_RREQ_retries", "adp_RLC_enabled", "adp_net_traversal_time", "adp_routing_table_entry_TTL", "adp_RREQ_RERR_wait", "adp_Blacklist_table_entry_TTL", "adp_unicast_RREQ_gen_enable", "adp_add_rev_link_cost"))
                .put(messageSpec(PLCConfigurationDeviceMessage.SetBroadCastLogTableEntryTTLAttributeName), new MultipleAttributeMessageEntry("SetBroadcastLogTableEntryTTL", "BroadcastLogTableEntryTTL"))
                .put(messageSpec(PLCConfigurationDeviceMessage.SetMaxJoinWaitTime), new MultipleAttributeMessageEntry("SetMaxJoinWaitTime", "MaxJoinWaitTime"))
                .put(messageSpec(PLCConfigurationDeviceMessage.SetPathDiscoveryTime), new MultipleAttributeMessageEntry("SetPathDiscoveryTime", "PathDiscoveryTime"))
                .put(messageSpec(PLCConfigurationDeviceMessage.SetMetricType), new MultipleAttributeMessageEntry("SetMetricType", "MetricType"))
                .put(messageSpec(PLCConfigurationDeviceMessage.SetCoordShortAddress), new MultipleAttributeMessageEntry("SetCoordShortAddress", "CoordShortAddress"))
                .put(messageSpec(PLCConfigurationDeviceMessage.SetDisableDefaultRouting), new MultipleAttributeMessageEntry("SetDisableDefaultRouting", "DisableDefaultRouting"))
                .put(messageSpec(PLCConfigurationDeviceMessage.SetDeviceType), new MultipleAttributeMessageEntry("SetDeviceType", "DeviceType"))

                .put(messageSpec(PLCConfigurationDeviceMessage.ResetPlcOfdmMacCounters), new SimpleTagMessageEntry("ResetPlcOfdmMacCounters"))
                .put(messageSpec(PLCConfigurationDeviceMessage.SetToneMaskAttributeName), new MultipleAttributeMessageEntry("SetToneMask", "ToneMask"))
                .put(messageSpec(PLCConfigurationDeviceMessage.SetTMRTTL), new MultipleAttributeMessageEntry("SetTMRTTL", "tmrTTL"))
                .put(messageSpec(PLCConfigurationDeviceMessage.SetMaxFrameRetries), new MultipleAttributeMessageEntry("SetMaxFrameRetries", "maxFrameRetries"))
                .put(messageSpec(PLCConfigurationDeviceMessage.SetNeighbourTableEntryTTL), new MultipleAttributeMessageEntry("SetNeighbourTableEntryTTL", "NeighbourTableEntryTTL"))
                .put(messageSpec(PLCConfigurationDeviceMessage.SetHighPriorityWindowSize), new MultipleAttributeMessageEntry("SetHighPriorityWindowSize", "windowSize"))
                .put(messageSpec(PLCConfigurationDeviceMessage.SetCSMAFairnessLimit), new MultipleAttributeMessageEntry("SetCSMAFairnessLimit", "CSMAFairnessLimit"))
                .put(messageSpec(PLCConfigurationDeviceMessage.SetBeaconRandomizationWindowLength), new MultipleAttributeMessageEntry("SetBeaconRandomizationWindowLength", "WindowLength"))
                .put(messageSpec(PLCConfigurationDeviceMessage.SetMacA), new MultipleAttributeMessageEntry("SetMacA", "MAC_A"))
                .put(messageSpec(PLCConfigurationDeviceMessage.SetMacK), new MultipleAttributeMessageEntry("SetMacK", "MAC_K"))
                .put(messageSpec(PLCConfigurationDeviceMessage.SetMinimumCWAttempts), new MultipleAttributeMessageEntry("SetMinimumCWAttempts", "minimumCWAttempts"))
                .put(messageSpec(PLCConfigurationDeviceMessage.SetMaxBe), new MultipleAttributeMessageEntry("SetMaxBe", "maxBE"))
                .put(messageSpec(PLCConfigurationDeviceMessage.SetMaxCSMABackOff), new MultipleAttributeMessageEntry("SetMaxCSMABackOff", "maxCSMABackOff"))
                .put(messageSpec(PLCConfigurationDeviceMessage.SetMinBe), new MultipleAttributeMessageEntry("SetMinBe", "minBE"))
                .put(messageSpec(PLCConfigurationDeviceMessage.PathRequest), new MultipleAttributeMessageEntry("PathRequest", "groupId"))
                .put(messageSpec(PLCConfigurationDeviceMessage.WritePlcG3Timeout), new MultipleAttributeMessageEntry("WritePlcG3Timeout", "Timeout_in_minutes"))

                .put(messageSpec(LogBookDeviceMessage.ResetMainLogbook), new SimpleTagMessageEntry("ResetMainLogbook"))
                .put(messageSpec(LogBookDeviceMessage.ResetCoverLogbook), new SimpleTagMessageEntry("ResetCoverLogbook"))
                .put(messageSpec(LogBookDeviceMessage.ResetBreakerLogbook), new SimpleTagMessageEntry("ResetBreakerLogbook"))
                .put(messageSpec(LogBookDeviceMessage.ResetCommunicationLogbook), new SimpleTagMessageEntry("ResetCommunicationLogbook"))
                .put(messageSpec(LogBookDeviceMessage.ResetVoltageCutLogbook), new SimpleTagMessageEntry("ResetVoltageCutLogbook"))
                .put(messageSpec(LogBookDeviceMessage.ResetLQILogbook), new SimpleTagMessageEntry("ResetLQILogbook"))
                .put(messageSpec(LogBookDeviceMessage.ResetSecurityLogbook), new SimpleTagMessageEntry("ResetSecurityLogbook"))

                .put(messageSpec(LoadProfileMessage.ResetActiveImportLP), new SimpleTagMessageEntry("ResetActiveImportLP"))
                .put(messageSpec(LoadProfileMessage.ResetActiveExportLP), new SimpleTagMessageEntry("ResetActiveExportLP"))
                .put(messageSpec(LoadProfileMessage.ResetDailyProfile), new SimpleTagMessageEntry("ResetDailyProfile"))
                .put(messageSpec(LoadProfileMessage.ResetMonthlyProfile), new SimpleTagMessageEntry("ResetMonthlyProfile"))

                .put(messageSpec(LoadProfileMessage.WRITE_CAPTURE_PERIOD_LP1), new MultipleAttributeMessageEntry("WriteProfileInterval", "IntervalInSeconds"))
                .put(messageSpec(LoadProfileMessage.WriteConsumerProducerMode), new MultipleAttributeMessageEntry("WriteConsumerProducerMode", "Mode"))

                .put(messageSpec(AdvancedTestMessage.LogObjectList), new SimpleTagMessageEntry("LogObjectList"))
                .put(messageSpec(ClockDeviceMessage.SyncTime), new SimpleTagMessageEntry("ForceSyncClock"))
                .put(messageSpec(ClockDeviceMessage.SET_TIME), new MultipleAttributeMessageEntry("WriteClockDateTime", "DateTime"))

                .put(messageSpec(SecurityMessage.CHANGE_DLMS_AUTHENTICATION_LEVEL), new MultipleAttributeMessageEntry("ChangeAuthenticationLevel", "Authentication_level"))
                .put(messageSpec(SecurityMessage.ACTIVATE_DLMS_ENCRYPTION), new MultipleAttributeMessageEntry(RtuMessageConstant.AEE_ACTIVATE_SECURITY, "Security_level"))
                .put(messageSpec(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEYS), new MultipleAttributeMessageEntry(RtuMessageConstant.NTA_AEE_CHANGE_DATATRANSPORT_AUTHENTICATION_KEY, "NewAuthenticationKey", "NewWrappedAuthenticationKey"))
                .put(messageSpec(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS), new MultipleAttributeMessageEntry(RtuMessageConstant.NTA_AEE_CHANGE_DATATRANSPORT_ENCRYPTION_KEY, "NewEncryptionKey", "NewWrappedEncryptionKey"))
                .put(messageSpec(SecurityMessage.CHANGE_HLS_SECRET_HEX), new MultipleAttributeMessageEntry(RtuMessageConstant.AEE_CHANGE_HLS_SECRET, "HLS_Secret"))
                .put(messageSpec(SecurityMessage.CHANGE_LLS_SECRET_HEX), new MultipleAttributeMessageEntry("ChangeLLSSecret", "LLS_Secret"))
                .put(messageSpec(SecurityMessage.WRITE_PSK), new MultipleAttributeMessageEntry("WritePlcPsk", "PSK"))

                .put(messageSpec(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_TYPE), new ActivityCalendarMessageEntry(activityCalendarTypeAttributeName, activityCalendarNameAttributeName, activityCalendarActivationDateAttributeName, activityCalendarCodeTableAttributeName))
                .put(messageSpec(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND_WITH_TYPE), new SpecialDaysMessageEntry(activityCalendarTypeAttributeName, specialDaysCodeTableAttributeName))

                .put(messageSpec(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_RESUME_OPTION_AND_TYPE), new FirmwareUdateWithUserFileMessageEntry(firmwareUpdateUserFileAttributeName, resumeFirmwareUpdateAttributeName, plcTypeFirmwareUpdateAttributeName))
                .build();
    }
}