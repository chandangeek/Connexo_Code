package com.energyict.protocolimplv2.nta.dsmr23.messages;

import com.energyict.cbo.Password;
import com.energyict.cbo.TimeDuration;
import com.energyict.cpo.PropertySpec;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdc.protocol.tasks.support.DeviceMessageSupport;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.core.CodeCalendar;
import com.energyict.mdw.core.LoadProfile;
import com.energyict.mdw.core.Lookup;
import com.energyict.mdw.core.LookupEntry;
import com.energyict.mdw.core.UserFile;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.AdvancedTestMessage;
import com.energyict.protocolimplv2.messages.ClockDeviceMessage;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DisplayDeviceMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadBalanceDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadProfileMessage;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.messages.convertor.utils.LoadProfileMessageUtils;
import com.energyict.protocolimplv2.messages.enums.DlmsAuthenticationLevelMessageValues;
import com.energyict.protocolimplv2.messages.enums.DlmsEncryptionLevelMessageValues;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractNtaMessageExecutor;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractNtaMessaging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;

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
public class Dsmr23Messaging extends AbstractNtaMessaging implements DeviceMessageSupport {

    private final static List<DeviceMessageSpec> supportedMessages;
    public static final String SEPARATOR = ";";

    static {
        supportedMessages = new ArrayList<>();

        // contactor related
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_OPEN);
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE);
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_CLOSE);
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE);
        supportedMessages.add(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE);

        // firmware upgrade related
        supportedMessages.add(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE);
        supportedMessages.add(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE);

        // activity calendar related
        supportedMessages.add(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND);
        supportedMessages.add(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME);
        supportedMessages.add(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND);

        // security related
        supportedMessages.add(SecurityMessage.ACTIVATE_DLMS_ENCRYPTION);
        supportedMessages.add(SecurityMessage.CHANGE_DLMS_AUTHENTICATION_LEVEL);
        supportedMessages.add(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY);
        supportedMessages.add(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY);
        supportedMessages.add(SecurityMessage.CHANGE_PASSWORD_WITH_NEW_PASSWORD);

        // network and connectivity
        supportedMessages.add(NetworkConnectivityMessage.ACTIVATE_WAKEUP_MECHANISM);
        supportedMessages.add(NetworkConnectivityMessage.DEACTIVATE_SMS_WAKEUP);
        supportedMessages.add(NetworkConnectivityMessage.CHANGE_GPRS_USER_CREDENTIALS);
        supportedMessages.add(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS);
        supportedMessages.add(NetworkConnectivityMessage.ADD_PHONENUMBERS_TO_WHITE_LIST);

        // display P1
        supportedMessages.add(DisplayDeviceMessage.CONSUMER_MESSAGE_CODE_TO_PORT_P1);
        supportedMessages.add(DisplayDeviceMessage.CONSUMER_MESSAGE_TEXT_TO_PORT_P1);

        // Device Actions
        supportedMessages.add(DeviceActionMessage.GLOBAL_METER_RESET);

        // Load balance
        supportedMessages.add(LoadBalanceDeviceMessage.CONFIGURE_LOAD_LIMIT_PARAMETERS);
        supportedMessages.add(LoadBalanceDeviceMessage.SET_EMERGENCY_PROFILE_GROUP_IDS);
        supportedMessages.add(LoadBalanceDeviceMessage.CLEAR_LOAD_LIMIT_CONFIGURATION);

        // Advanced test
        supportedMessages.add(AdvancedTestMessage.XML_CONFIG);

        // LoadProfiles
        supportedMessages.add(LoadProfileMessage.PARTIAL_LOAD_PROFILE_REQUEST);
        supportedMessages.add(LoadProfileMessage.LOAD_PROFILE_REGISTER_REQUEST);

        // clock related
        supportedMessages.add(ClockDeviceMessage.SET_TIME);

        // reset
        supportedMessages.add(ConfigurationChangeDeviceMessage.ChangeDefaultResetWindow);
        supportedMessages.add(DeviceActionMessage.ALARM_REGISTER_RESET);
    }

    private final AbstractNtaMessageExecutor messageExecutor;

    public Dsmr23Messaging(AbstractNtaMessageExecutor messageExecutor) {
        super(messageExecutor.getProtocol());
        this.messageExecutor = messageExecutor;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return supportedMessages;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        switch (propertySpec.getName()) {
            case UserFileConfigAttributeName:
            case firmwareUpdateUserFileAttributeName:
                return ProtocolTools.getHexStringFromBytes(((UserFile) messageAttribute).loadFileInByteArray(), "");
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
                return LoadProfileMessageUtils.formatLoadProfile((LoadProfile) messageAttribute);
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
                    throw MdcManager.getComServerExceptionFactory().createGeneralParseException(e);
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


    public AbstractNtaMessageExecutor getMessageExecutor() {
        return messageExecutor;
    }
}
