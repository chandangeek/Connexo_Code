package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;

import com.energyict.cbo.HexString;
import com.energyict.cbo.TimeDuration;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.core.UserFile;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.GeneralDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadBalanceDeviceMessage;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.messages.PLCConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.AS220ActivityCalendarMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.FirmwareUdateWithUserFileMessageEntry;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.ActiveChannelAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.CHANNEL1_CREDITWEIGHTAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.CHANNEL1_FMAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.CHANNEL1_FSAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.CHANNEL1_SNRAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.CHANNEL2_CREDITWEIGHTAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.CHANNEL2_FMAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.CHANNEL2_FSAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.CHANNEL2_SNRAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.CHANNEL3_CREDITWEIGHTAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.CHANNEL3_FMAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.CHANNEL3_FSAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.CHANNEL3_SNRAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.CHANNEL4_CREDITWEIGHTAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.CHANNEL4_FMAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.CHANNEL4_FSAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.CHANNEL4_SNRAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.CHANNEL5_CREDITWEIGHTAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.CHANNEL5_FMAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.CHANNEL5_FSAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.CHANNEL5_SNRAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.CHANNEL6_CREDITWEIGHTAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.CHANNEL6_FMAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.CHANNEL6_FSAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.CHANNEL6_SNRAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.IEC1107ClassIdAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.INITIATOR_ELECTRICAL_PHASEAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.MAX_FRAME_LENGTHAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.MAX_RECEIVING_GAINAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.MAX_TRANSMITTING_GAINAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.OffsetAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.REPEATERAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.RawDataAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.SEARCH_INITIATOR_GAINAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.SEARCH_INITIATOR_TIMEOUTAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.SYNCHRONIZATION_CONFIRMATION_TIMEOUTAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.TIME_OUT_FRAME_NOT_OKAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.TIME_OUT_NOT_ADDRESSEDAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarCodeTableAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarNameAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateUserFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.normalThresholdAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.overThresholdDurationAttributeName;

/**
 * Represents a MessageConverter for the DLMS AS220 protocol
 * <p/>
 * Copyrights EnergyICT
 * Date: 8/03/13
 * Time: 16:26
 */
public class AS220DLMSMessageConverter extends AbstractMessageConverter {

    /**
     * Represents a mapping between {@link DeviceMessageSpec deviceMessageSpecs}
     * and the corresponding {@link com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator}
     */
    private static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>();

    static {
        registry.put(ActivityCalendarDeviceMessage.ACTIVATE_PASSIVE_CALENDAR, new MultipleAttributeMessageEntry("ActivatePassiveCalendar", "ActivationTime"));
        registry.put(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND, new AS220ActivityCalendarMessageEntry(activityCalendarNameAttributeName, activityCalendarActivationDateAttributeName, activityCalendarCodeTableAttributeName));
        registry.put(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME, new AS220ActivityCalendarMessageEntry(activityCalendarNameAttributeName, activityCalendarActivationDateAttributeName, activityCalendarCodeTableAttributeName));

        registry.put(ContactorDeviceMessage.CONTACTOR_CLOSE, new SimpleTagMessageEntry("ConnectEmeter"));
        registry.put(ContactorDeviceMessage.CONTACTOR_OPEN, new SimpleTagMessageEntry("DisconnectEmeter"));
        registry.put(LoadBalanceDeviceMessage.SET_LOAD_LIMIT_DURATION, new MultipleAttributeMessageEntry("SetLoadLimitDuration", "LoadLimitDuration"));
        registry.put(LoadBalanceDeviceMessage.SET_LOAD_LIMIT_THRESHOLD, new MultipleAttributeMessageEntry("SetLoadLimitThreshold", "LoadLimitThreshold"));
        registry.put(GeneralDeviceMessage.WRITE_RAW_IEC1107_CLASS, new MultipleAttributeMessageEntry("WriteRawIEC1107Class", "IEC1107ClassId", "Offset", "RawData"));

        registry.put(PLCConfigurationDeviceMessage.ForceManualRescanPLCBus, new SimpleTagMessageEntry("RescanPlcBus"));
        registry.put(PLCConfigurationDeviceMessage.SetActivePlcChannel, new MultipleAttributeMessageEntry("SetActivePlcChannel", "ACTIVE_CHANNEL"));
        registry.put(PLCConfigurationDeviceMessage.SetPlcChannelFrequencies, new MultipleAttributeMessageEntry("SetPlcChannelFrequencies", getChannelFrequencyTags()));
        registry.put(PLCConfigurationDeviceMessage.SetPlcChannelFreqSnrCredits, new MultipleAttributeMessageEntry("SetPlcChannelFreqSnrCredits", getChannelFreqSnrCreditsTags()));
        registry.put(PLCConfigurationDeviceMessage.SetSFSKGain, new MultipleAttributeMessageEntry("SetSFSKGain", "MAX_RECEIVING_GAIN", "MAX_TRANSMITTING_GAIN", "SEARCH_INITIATOR_GAIN"));
        registry.put(PLCConfigurationDeviceMessage.SetSFSKInitiatorPhase, new MultipleAttributeMessageEntry("SetSFSKInitiatorPhase", "INITIATOR_ELECTRICAL_PHASE"));
        registry.put(PLCConfigurationDeviceMessage.SetSFSKMacTimeouts, new MultipleAttributeMessageEntry("SetSFSKMacTimeouts", "SEARCH_INITIATOR_TIMEOUT", "SYNCHRONIZATION_CONFIRMATION_TIMEOUT", "TIME_OUT_NOT_ADDRESSED", "TIME_OUT_FRAME_NOT_OK"));
        registry.put(PLCConfigurationDeviceMessage.SetSFSKMaxFrameLength, new MultipleAttributeMessageEntry("SetSFSKMaxFrameLength", "MAX_FRAME_LENGTH"));
        registry.put(PLCConfigurationDeviceMessage.SetSFSKRepeater, new MultipleAttributeMessageEntry("SetSFSKRepeater", "REPEATER"));

        registry.put(MBusSetupDeviceMessage.DecommissionAll, new SimpleTagMessageEntry("DecommissionAll"));
        registry.put(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE, new FirmwareUdateWithUserFileMessageEntry(firmwareUpdateUserFileAttributeName));
    }

    private static String[] getChannelFrequencyTags() {
        return new String[]{
                "CHANNEL1_FS",
                "CHANNEL1_FM",
                "CHANNEL2_FS",
                "CHANNEL2_FM",
                "CHANNEL3_FS",
                "CHANNEL3_FM",
                "CHANNEL4_FS",
                "CHANNEL4_FM",
                "CHANNEL5_FS",
                "CHANNEL5_FM",
                "CHANNEL6_FS",
                "CHANNEL6_FM"};
    }

    private static String[] getChannelFreqSnrCreditsTags() {
        return new String[]{
                "CHANNEL1_FS",
                "CHANNEL1_FM",
                "CHANNEL1_SNR",
                "CHANNEL1_CREDITWEIGHT",
                "CHANNEL2_FS",
                "CHANNEL2_FM",
                "CHANNEL2_SNR",
                "CHANNEL2_CREDITWEIGHT",
                "CHANNEL3_FS",
                "CHANNEL3_FM",
                "CHANNEL3_SNR",
                "CHANNEL3_CREDITWEIGHT",
                "CHANNEL4_FS",
                "CHANNEL4_FM",
                "CHANNEL4_SNR",
                "CHANNEL4_CREDITWEIGHT",
                "CHANNEL5_FS",
                "CHANNEL5_FM",
                "CHANNEL5_SNR",
                "CHANNEL5_CREDITWEIGHT",
                "CHANNEL6_FS",
                "CHANNEL6_FM",
                "CHANNEL6_SNR",
                "CHANNEL6_CREDITWEIGHT"};
    }

    /**
     * Default constructor for at-runtime instantiation
     */
    public AS220DLMSMessageConverter() {
        super();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {

        if (propertySpec.getName().equals(activityCalendarActivationDateAttributeName)) {
            return dateTimeFormat.format((Date) messageAttribute);
        } else if (propertySpec.getName().equals(activityCalendarCodeTableAttributeName)) {
            return convertCodeTableToXML((Code) messageAttribute);
        } else if (propertySpec.getName().equals(overThresholdDurationAttributeName)) {
            return String.valueOf(((TimeDuration) messageAttribute).getSeconds());
        } else if (propertySpec.getName().equals(normalThresholdAttributeName)
                || propertySpec.getName().equals(OffsetAttributeName)
                || propertySpec.getName().equals(ActiveChannelAttributeName)
                || propertySpec.getName().equals(CHANNEL1_FSAttributeName)
                || propertySpec.getName().equals(CHANNEL1_FMAttributeName)
                || propertySpec.getName().equals(CHANNEL2_FSAttributeName)
                || propertySpec.getName().equals(CHANNEL2_FMAttributeName)
                || propertySpec.getName().equals(CHANNEL3_FSAttributeName)
                || propertySpec.getName().equals(CHANNEL3_FMAttributeName)
                || propertySpec.getName().equals(CHANNEL4_FSAttributeName)
                || propertySpec.getName().equals(CHANNEL4_FMAttributeName)
                || propertySpec.getName().equals(CHANNEL5_FSAttributeName)
                || propertySpec.getName().equals(CHANNEL5_FMAttributeName)
                || propertySpec.getName().equals(CHANNEL6_FSAttributeName)
                || propertySpec.getName().equals(CHANNEL6_FMAttributeName)
                || propertySpec.getName().equals(CHANNEL1_SNRAttributeName)
                || propertySpec.getName().equals(CHANNEL1_CREDITWEIGHTAttributeName)
                || propertySpec.getName().equals(CHANNEL2_SNRAttributeName)
                || propertySpec.getName().equals(CHANNEL2_CREDITWEIGHTAttributeName)
                || propertySpec.getName().equals(CHANNEL3_SNRAttributeName)
                || propertySpec.getName().equals(CHANNEL3_CREDITWEIGHTAttributeName)
                || propertySpec.getName().equals(CHANNEL4_SNRAttributeName)
                || propertySpec.getName().equals(CHANNEL4_CREDITWEIGHTAttributeName)
                || propertySpec.getName().equals(CHANNEL5_SNRAttributeName)
                || propertySpec.getName().equals(CHANNEL5_CREDITWEIGHTAttributeName)
                || propertySpec.getName().equals(CHANNEL6_SNRAttributeName)
                || propertySpec.getName().equals(CHANNEL6_CREDITWEIGHTAttributeName)
                || propertySpec.getName().equals(MAX_RECEIVING_GAINAttributeName)
                || propertySpec.getName().equals(MAX_TRANSMITTING_GAINAttributeName)
                || propertySpec.getName().equals(SEARCH_INITIATOR_GAINAttributeName)
                || propertySpec.getName().equals(INITIATOR_ELECTRICAL_PHASEAttributeName)
                || propertySpec.getName().equals(SEARCH_INITIATOR_TIMEOUTAttributeName)
                || propertySpec.getName().equals(SYNCHRONIZATION_CONFIRMATION_TIMEOUTAttributeName)
                || propertySpec.getName().equals(TIME_OUT_NOT_ADDRESSEDAttributeName)
                || propertySpec.getName().equals(TIME_OUT_FRAME_NOT_OKAttributeName)
                || propertySpec.getName().equals(MAX_FRAME_LENGTHAttributeName)
                || propertySpec.getName().equals(REPEATERAttributeName)
                || propertySpec.getName().equals(IEC1107ClassIdAttributeName)) {
            return messageAttribute.toString();
        } else if (propertySpec.getName().equals(RawDataAttributeName)) {
            return ((HexString) messageAttribute).getContent();
        } else if (propertySpec.getName().equals(firmwareUpdateUserFileAttributeName)) {
            UserFile userFile = (UserFile) messageAttribute;
            return new String(userFile.loadFileInByteArray());
        }
        return EMPTY_FORMAT;
    }

    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return registry;
    }
}