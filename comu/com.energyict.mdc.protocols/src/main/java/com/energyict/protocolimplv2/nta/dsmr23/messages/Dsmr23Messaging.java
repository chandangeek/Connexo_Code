package com.energyict.protocolimplv2.nta.dsmr23.messages;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.ExceptionalOccurrence;
import com.elster.jupiter.calendar.FixedExceptionalOccurrence;
import com.elster.jupiter.calendar.RecurrentExceptionalOccurrence;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.mdc.common.Password;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.messages.DlmsAuthenticationLevelMessageValues;
import com.energyict.mdc.protocol.api.device.messages.DlmsEncryptionLevelMessageValues;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.exceptions.GeneralParseException;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.tasks.support.DeviceMessageSupport;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.messages.convertor.utils.LoadProfileMessageUtils;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractDlmsMessaging;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.*;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.UserFileConfigAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.activityCalendarActivationDateAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.activityCalendarAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.authenticationLevelAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.contactorActivationDateAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.encryptionLevelAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.firmwareUpdateActivationDateAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.firmwareUpdateFileAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.fromDateAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.loadProfileAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.meterTimeAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.newAuthenticationKeyAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.newEncryptionKeyAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.newPasswordAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.overThresholdDurationAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.specialDaysAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.toDateAttributeName;

/**
 * Class that:
 * - Formats the device message attributes from objects to proper string values
 * - Executes a given message
 * - Has a list of all supported device message specs
 * <p>
 * Copyrights EnergyICT
 * Date: 22/11/13
 * Time: 11:32
 * Author: khe
 */
public class Dsmr23Messaging extends AbstractDlmsMessaging implements DeviceMessageSupport {

    public static final String SEPARATOR = ";";
    private final Set<DeviceMessageId> supportedMessages = EnumSet.of(
            DeviceMessageId.CONTACTOR_OPEN,
            DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE,
            DeviceMessageId.CONTACTOR_CLOSE,
            DeviceMessageId.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE,
            DeviceMessageId.CONTACTOR_CHANGE_CONNECT_CONTROL_MODE,
            DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE,
            DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_ACTIVATE_DATE,
            DeviceMessageId.ACTIVITY_CALENDER_SEND,
            DeviceMessageId.ACTIVITY_CALENDER_SEND_WITH_DATETIME,
            DeviceMessageId.ACTIVITY_CALENDAR_SPECIAL_DAY_CALENDAR_SEND,
            DeviceMessageId.SECURITY_ACTIVATE_DLMS_ENCRYPTION,
            DeviceMessageId.SECURITY_CHANGE_DLMS_AUTHENTICATION_LEVEL,
            DeviceMessageId.SECURITY_CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY,
            DeviceMessageId.SECURITY_CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY,
            DeviceMessageId.SECURITY_CHANGE_PASSWORD_WITH_NEW_PASSWORD,
            DeviceMessageId.NETWORK_CONNECTIVITY_ACTIVATE_WAKEUP_MECHANISM,
            DeviceMessageId.NETWORK_CONNECTIVITY_DEACTIVATE_SMS_WAKEUP,
            DeviceMessageId.NETWORK_CONNECTIVITY_CHANGE_GPRS_USER_CREDENTIALS,
            DeviceMessageId.NETWORK_CONNECTIVITY_CHANGE_GPRS_APN_CREDENTIALS,
            DeviceMessageId.NETWORK_CONNECTIVITY_ADD_PHONENUMBERS_TO_WHITE_LIST,
            DeviceMessageId.DISPLAY_CONSUMER_MESSAGE_CODE_TO_PORT_P1,
            DeviceMessageId.DISPLAY_CONSUMER_MESSAGE_TEXT_TO_PORT_P1,
            DeviceMessageId.DEVICE_ACTIONS_GLOBAL_METER_RESET,
            DeviceMessageId.LOAD_BALANCING_CONFIGURE_LOAD_LIMIT_PARAMETERS,
            DeviceMessageId.LOAD_BALANCING_SET_EMERGENCY_PROFILE_GROUP_IDS,
            DeviceMessageId.LOAD_BALANCING_CLEAR_LOAD_LIMIT_CONFIGURATION,
            DeviceMessageId.ADVANCED_TEST_XML_CONFIG,
            DeviceMessageId.CLOCK_SET_TIME,
            DeviceMessageId.CONFIGURATION_CHANGE_CHANGE_DEFAULT_RESET_WINDOW,
            DeviceMessageId.DEVICE_ACTIONS_ALARM_REGISTER_RESET,
            DeviceMessageId.LOAD_PROFILE_PARTIAL_REQUEST,
            DeviceMessageId.LOAD_PROFILE_REGISTER_REQUEST
    );
    private final AbstractMessageExecutor messageExecutor;
    private final TopologyService topologyService;
    /**
     * Boolean indicating whether or not to show the MBus related messages in EIServer
     */
    protected boolean supportMBus = true;
    /**
     * Boolean indicating whether or not to show the GPRS related messages in EIServer
     */
    protected boolean supportGPRS = true;
    /**
     * Boolean indicating whether or not to show the messages related to resetting the meter in EIServer
     */
    protected boolean supportMeterReset = true;
    /**
     * Boolean indicating whether or not to show the messages related to configuring the limiter in EIServer
     */
    protected boolean supportLimiter = true;
    /**
     * Boolean indicating whether or not to show the message to reset the alarm window in EIServer
     */
    protected boolean supportResetWindow = true;

    public Dsmr23Messaging(AbstractMessageExecutor messageExecutor, TopologyService topologyService) {
        super(messageExecutor.getProtocol());
        this.messageExecutor = messageExecutor;
        this.topologyService = topologyService;
    }

    @Override
    public Set<DeviceMessageId> getSupportedMessages() {
        return supportedMessages;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        switch (propertySpec.getName()) {
            case firmwareUpdateFileAttributeName:
                return messageAttribute.toString();     //This is the path of the temp file representing the FirmwareVersion
            case activityCalendarAttributeName:
                return convertCodeTableToXML((Calendar) messageAttribute);
            case authenticationLevelAttributeName:
                return String.valueOf(DlmsAuthenticationLevelMessageValues.getValueFor(messageAttribute.toString()));
            case encryptionLevelAttributeName:
                return String.valueOf(DlmsEncryptionLevelMessageValues.getValueFor(messageAttribute.toString()));
            case overThresholdDurationAttributeName:
                return String.valueOf(((TimeDuration) messageAttribute).getSeconds());
            case newEncryptionKeyAttributeName:
            case newPasswordAttributeName:
            case newAuthenticationKeyAttributeName:
                return ((Password) messageAttribute).getValue();
            case meterTimeAttributeName:
                return String.valueOf(((Date) messageAttribute).getTime());
            case specialDaysAttributeName:
                return parseSpecialDays(((Calendar) messageAttribute));
            case loadProfileAttributeName:
                return LoadProfileMessageUtils.formatLoadProfile((LoadProfile) messageAttribute, this.topologyService);
            case fromDateAttributeName:
            case toDateAttributeName:
                return String.valueOf(((Date) messageAttribute).getTime());
            case contactorActivationDateAttributeName:
            case activityCalendarActivationDateAttributeName:
            case firmwareUpdateActivationDateAttributeName:
                return String.valueOf(((Date) messageAttribute).getTime());  //Epoch (millis)
            default:
                return messageAttribute.toString();  //Used for String and BigDecimal attributes
        }
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return getMessageExecutor().updateSentMessages(sentMessages);
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getMessageExecutor().executePendingMessages(pendingMessages);
    }

    public void setSupportMBus(boolean supportMBus) {
        this.supportMBus = supportMBus;
    }

    public void setSupportGPRS(boolean supportGPRS) {
        this.supportGPRS = supportGPRS;
    }

    public void setSupportMeterReset(boolean supportMeterReset) {
        this.supportMeterReset = supportMeterReset;
    }

    public void setSupportLimiter(boolean supportsLimiter) {
        this.supportLimiter = supportsLimiter;
    }

    public void setSupportResetWindow(boolean supportResetWindow) {
        this.supportResetWindow = supportResetWindow;
    }

    /**
     * Parse the special days of the given code table into the proper AXDR array.
     */
    private String parseSpecialDays(Calendar calendar) {
        List<ExceptionalOccurrence> exceptionalOccurrences = calendar.getExceptionalOccurrences();
        Array result = new Array();
        for (ExceptionalOccurrence exceptionalOccurrence : exceptionalOccurrences) {
            byte[] timeStampBytes;
            if (exceptionalOccurrence instanceof FixedExceptionalOccurrence) {
                timeStampBytes = this.getTimestampBytes((FixedExceptionalOccurrence) exceptionalOccurrence);
            } else {
                timeStampBytes = this.getTimestampBytes((RecurrentExceptionalOccurrence) exceptionalOccurrence);
            }
            OctetString os = OctetString.fromByteArray(timeStampBytes);
            Unsigned8 dayType = new Unsigned8((int) exceptionalOccurrence.getDayType().getId());
            Structure struct = new Structure();
            AXDRDateTime dt;
            try {
                if (exceptionalOccurrence instanceof FixedExceptionalOccurrence) {
                    dt = this.newAXDRDateTimeFrom((FixedExceptionalOccurrence) exceptionalOccurrence);
                } else {
                    dt = this.newAXDRDateTimeFrom((RecurrentExceptionalOccurrence) exceptionalOccurrence);
                }
            } catch (ProtocolException e) {
                throw new GeneralParseException(MessageSeeds.GENERAL_PARSE_ERROR, e);
            }
            long days = dt.getValue().getTimeInMillis() / 1000 / 60 / 60 / 24;
            struct.addDataType(new Unsigned16((int) days));
            struct.addDataType(os);
            struct.addDataType(dayType);
            result.addDataType(struct);
        }
        return ProtocolTools.getHexStringFromBytes(result.getBEREncodedByteArray(), "");
    }

    private byte[] getTimestampBytes(FixedExceptionalOccurrence exceptionalOccurrence) {
        return new byte[]{
                (byte) ((exceptionalOccurrence.getOccurrence().getYear() >> 8) & 0xFF),
                (byte) ((exceptionalOccurrence.getOccurrence().getYear() & 0xFF)),
                (byte) exceptionalOccurrence.getOccurrence().getMonthValue(),
                (byte) exceptionalOccurrence.getOccurrence().getDayOfMonth(),
                (byte) 0xFF};
    }

    private byte[] getTimestampBytes(RecurrentExceptionalOccurrence exceptionalOccurrence) {
        return new byte[]{
                (byte) 0xff,
                (byte) 0xff,
                (byte) exceptionalOccurrence.getOccurrence().getMonthValue(),
                (byte) exceptionalOccurrence.getOccurrence().getDayOfMonth(),
                (byte) 0xFF};
    }

    private AXDRDateTime newAXDRDateTimeFrom(FixedExceptionalOccurrence exceptionalOccurrence) throws ProtocolException {
        return new AXDRDateTime(new byte[]{
                (byte) 0x09,
                (byte) 0x0C,
                (byte) ((exceptionalOccurrence.getOccurrence().getYear() >> 8) & 0xFF),
                (byte) ((exceptionalOccurrence.getOccurrence().getYear()) & 0xFF),
                (byte) exceptionalOccurrence.getOccurrence().getMonthValue(),
                (byte) exceptionalOccurrence.getOccurrence().getDayOfMonth(),
                (byte) 0xFF,
                0, 0, 0, 0, 0, 0, 0});
    }

    private AXDRDateTime newAXDRDateTimeFrom(RecurrentExceptionalOccurrence exceptionalOccurrence) throws ProtocolException {
        return new AXDRDateTime(new byte[]{
                (byte) 0x09,
                (byte) 0x0C,
                (byte) 0x07,
                (byte) 0xB2,
                (byte) exceptionalOccurrence.getOccurrence().getMonthValue(),
                (byte) exceptionalOccurrence.getOccurrence().getDayOfMonth(),
                (byte) 0xFF,
                0, 0, 0, 0, 0, 0, 0});
    }

    protected AbstractMessageExecutor getMessageExecutor() {
        return messageExecutor;
    }

    protected TopologyService getTopologyService() {
        return topologyService;
    }
}
