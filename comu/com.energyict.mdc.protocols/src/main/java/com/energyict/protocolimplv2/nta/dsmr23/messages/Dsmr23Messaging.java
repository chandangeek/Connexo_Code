package com.energyict.protocolimplv2.nta.dsmr23.messages;

import com.energyict.mdc.common.Password;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.protocol.api.codetables.Code;
import com.energyict.mdc.protocol.api.codetables.CodeCalendar;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.messages.DlmsAuthenticationLevelMessageValues;
import com.energyict.mdc.protocol.api.device.messages.DlmsEncryptionLevelMessageValues;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.exceptions.GeneralParseException;
import com.energyict.mdc.protocol.api.lookups.Lookup;
import com.energyict.mdc.protocol.api.lookups.LookupEntry;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.tasks.support.DeviceMessageSupport;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.messages.convertor.utils.LoadProfileMessageUtils;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractDlmsMessaging;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

import java.io.IOException;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.UserFileConfigAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.activityCalendarActivationDateAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.activityCalendarCodeTableAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.authenticationLevelAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.contactorActivationDateAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.emergencyProfileGroupIdListAttributeName;
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
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.specialDaysCodeTableAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.toDateAttributeName;

/**
 * Class that:
 * - Formats the device message attributes from objects to proper string values
 * - Executes a given message
 * - Has a list of all supported device message specs
 * <p/>
 * Copyrights EnergyICT
 * Date: 22/11/13
 * Time: 11:32
 * Author: khe
 */
public class Dsmr23Messaging extends AbstractDlmsMessaging implements DeviceMessageSupport {

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
    public static final String SEPARATOR = ";";

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

    private final AbstractMessageExecutor messageExecutor;
    private final TopologyService topologyService;

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
            case UserFileConfigAttributeName:
            case firmwareUpdateFileAttributeName:
                return ProtocolTools.getHexStringFromBytes(((FirmwareVersion) messageAttribute).getFirmwareFile(), "");
            case activityCalendarCodeTableAttributeName:
                return convertCodeTableToXML((Code) messageAttribute);
            case authenticationLevelAttributeName:
                return String.valueOf(DlmsAuthenticationLevelMessageValues.getValueFor(messageAttribute.toString()));
            case emergencyProfileGroupIdListAttributeName:
                return convertLookupTable((Lookup) messageAttribute);
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
            case specialDaysCodeTableAttributeName:
                return parseSpecialDays(((Code) messageAttribute));
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
    private String parseSpecialDays(Code codeTable) {
        List<CodeCalendar> calendars = codeTable.getCalendars();
        Array result = new Array();
        for (CodeCalendar cc : calendars) {
            if (cc.getSeason() == 0) {
                OctetString os = OctetString.fromByteArray(new byte[]{(byte) ((cc.getYear() == -1) ? 0xff : ((cc.getYear() >> 8) & 0xFF)), (byte) ((cc.getYear() == -1) ? 0xff : (cc.getYear()) & 0xFF),
                        (byte) ((cc.getMonth() == -1) ? 0xFF : cc.getMonth()), (byte) ((cc.getDay() == -1) ? 0xFF : cc.getDay()),
                        (byte) ((cc.getDayOfWeek() == -1) ? 0xFF : cc.getDayOfWeek())});
                Unsigned8 dayType = new Unsigned8(cc.getDayType().getId());
                Structure struct = new Structure();
                AXDRDateTime dt = null;
                try {
                    dt = new AXDRDateTime(new byte[]{(byte) 0x09, (byte) 0x0C, (byte) ((cc.getYear() == -1) ? 0x07 : ((cc.getYear() >> 8) & 0xFF)), (byte) ((cc.getYear() == -1) ? 0xB2 : (cc.getYear()) & 0xFF),
                            (byte) ((cc.getMonth() == -1) ? 0xFF : cc.getMonth()), (byte) ((cc.getDay() == -1) ? 0xFF : cc.getDay()),
                            (byte) ((cc.getDayOfWeek() == -1) ? 0xFF : cc.getDayOfWeek()), 0, 0, 0, 0, 0, 0, 0});
                } catch (IOException e) {
                    throw new GeneralParseException(MessageSeeds.GENERAL_PARSE_ERROR, e);
                }
                long days = dt.getValue().getTimeInMillis() / 1000 / 60 / 60 / 24;
                struct.addDataType(new Unsigned16((int) days));
                struct.addDataType(os);
                struct.addDataType(dayType);
                result.addDataType(struct);
            }
        }
        return ProtocolTools.getHexStringFromBytes(result.getBEREncodedByteArray(), "");
    }

    private String convertLookupTable(Lookup messageAttribute) {
        StringBuilder result = new StringBuilder();
        for (LookupEntry entry : messageAttribute.getEntries()) {
            if (result.length() > 0) {
                result.append(SEPARATOR);
            }
            result.append(entry.getKey());
        }
        return result.toString();
    }


    protected AbstractMessageExecutor getMessageExecutor() {
        return messageExecutor;
    }
}
