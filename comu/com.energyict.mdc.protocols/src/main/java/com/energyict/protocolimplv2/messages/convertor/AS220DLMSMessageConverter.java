package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.common.HexString;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.protocol.api.UserFile;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.FirmwareUdateWithUserFileMessageEntry;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.energyict.mdc.protocol.api.impl.device.messages.DeviceMessageConstants.*;

/**
 * Represents a MessageConverter for the DLMS AS220 protocol
 * <p/>
 * Copyrights EnergyICT
 * Date: 8/03/13
 * Time: 16:26
 */
public class AS220DLMSMessageConverter extends AbstractMessageConverter {

    /**
     * Represents a mapping between {@link DeviceMessageSpec}s
     * and the corresponding {@link com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator}
     */
    private static Map<DeviceMessageId, MessageEntryCreator> registry = new HashMap<>();

    static {
        registry.put(DeviceMessageId.ACTIVATE_CALENDAR_PASSIVE, new MultipleAttributeMessageEntry("ActivatePassiveCalendar", "ActivationTime"));
        registry.put(DeviceMessageId.CONTACTOR_CLOSE, new SimpleTagMessageEntry("ConnectEmeter"));
        registry.put(DeviceMessageId.CONTACTOR_OPEN, new SimpleTagMessageEntry("DisconnectEmeter"));
        registry.put(DeviceMessageId.LOAD_BALANCING_SET_LOAD_LIMIT_DURATION, new MultipleAttributeMessageEntry("SetLoadLimitDuration", "LoadLimitDuration"));
        registry.put(DeviceMessageId.LOAD_BALANCING_SET_LOAD_LIMIT_THRESHOLD, new MultipleAttributeMessageEntry("SetLoadLimitThreshold", "LoadLimitThreshold"));
        registry.put(DeviceMessageId.GENERAL_WRITE_RAW_IEC1107_CLASS, new MultipleAttributeMessageEntry("WriteRawIEC1107Class", "IEC1107ClassId", "Offset", "RawData"));

        registry.put(DeviceMessageId.PLC_CONFIGURATION_FORCE_MANUAL_RESCAN_PLC_BUS, new SimpleTagMessageEntry("RescanPlcBus"));
        registry.put(DeviceMessageId.PLC_CONFIGURATION_SET_ACTIVE_CHANNEL, new MultipleAttributeMessageEntry("SetActivePlcChannel", "ACTIVE_CHANNEL"));
        registry.put(DeviceMessageId.PLC_CONFIGURATION_SET_CHANNEL_FREQUENCIES, new MultipleAttributeMessageEntry("SetPlcChannelFrequencies", getChannelFrequencyTags()));
        registry.put(DeviceMessageId.PLC_CONFIGURATION_SET_PLC_CHANNEL_FREQ_SNR_CREDITS, new MultipleAttributeMessageEntry("SetPlcChannelFreqSnrCredits", getChannelFreqSnrCreditsTags()));
        registry.put(DeviceMessageId.PLC_CONFIGURATION_SET_SFSK_GAIN, new MultipleAttributeMessageEntry("SetSFSKGain", "MAX_RECEIVING_GAIN", "MAX_TRANSMITTING_GAIN", "SEARCH_INITIATOR_GAIN"));
        registry.put(DeviceMessageId.PLC_CONFIGURATION_SET_SFSK_INITIATOR_PHASE, new MultipleAttributeMessageEntry("SetSFSKInitiatorPhase", "INITIATOR_ELECTRICAL_PHASE"));
        registry.put(DeviceMessageId.PLC_CONFIGURATION_SET_SFSK_MAC_TIMEOUTS, new MultipleAttributeMessageEntry("SetSFSKMacTimeouts", "SEARCH_INITIATOR_TIMEOUT", "SYNCHRONIZATION_CONFIRMATION_TIMEOUT", "TIME_OUT_NOT_ADDRESSED", "TIME_OUT_FRAME_NOT_OK"));
        registry.put(DeviceMessageId.PLC_CONFIGURATION_SET_SFSK_MAX_FRAME_LENGTH, new MultipleAttributeMessageEntry("SetSFSKMaxFrameLength", "MAX_FRAME_LENGTH"));
        registry.put(DeviceMessageId.PLC_CONFIGURATION_SET_SFSK_REPEATER, new MultipleAttributeMessageEntry("SetSFSKRepeater", "REPEATER"));

        registry.put(DeviceMessageId.MBUS_SETUP_DECOMMISSION_ALL, new SimpleTagMessageEntry("DecommissionAll"));
        registry.put(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE, new FirmwareUdateWithUserFileMessageEntry(firmwareUpdateUserFileAttributeName));
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

    protected Map<DeviceMessageId, MessageEntryCreator> getRegistry() {
        return registry;
    }

}