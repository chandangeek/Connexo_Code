package com.energyict.protocolimplv2.edp.messages;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.protocol.api.codetables.Code;
import com.energyict.mdc.protocol.api.codetables.CodeCalendar;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.exceptions.GeneralParseException;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.tasks.support.DeviceMessageSupport;
import com.energyict.protocolimpl.generic.messages.GenericMessaging;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractDlmsMessaging;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

import java.io.IOException;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.*;

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
public class EDPMessaging extends AbstractDlmsMessaging implements DeviceMessageSupport {

    private final  Set<DeviceMessageId> supportedMessages = EnumSet.of(
            DeviceMessageId.CONTACTOR_CLOSE_RELAY,
            DeviceMessageId.CONTACTOR_OPEN_RELAY,
            DeviceMessageId.CONTACTOR_SET_RELAY_CONTROL_MODE,
            DeviceMessageId.PUBLIC_LIGHTING_SET_RELAY_OPERATING_MODE,
            DeviceMessageId.PUBLIC_LIGHTING_SET_TIME_SWITCHING_TABLE,
            DeviceMessageId.PUBLIC_LIGHTING_SET_THRESHOLD_OVER_CONSUMPTION,
            DeviceMessageId.PUBLIC_LIGHTING_SET_OVERALL_MINIMUM_THRESHOLD,
            DeviceMessageId.PUBLIC_LIGHTING_SET_OVERALL_MAXIMUM_THRESHOLD,
            DeviceMessageId.PUBLIC_LIGHTING_SET_RELAY_TIME_OFFSETS_TABLE,
            DeviceMessageId.PUBLIC_LIGHTING_WRITE_GPS_COORDINATES,
            DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE,
            DeviceMessageId.ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_CONTRACT,
            DeviceMessageId.ACTIVITY_CALENDER_SPECIAL_DAY_CALENDAR_SEND_WITH_CONTRACT_AND_DATETIME,
            DeviceMessageId.DEVICE_ACTIONS_BILLING_RESET,
            DeviceMessageId.DEVICE_ACTIONS_BILLING_RESET_CONTRACT_1,
            DeviceMessageId.DEVICE_ACTIONS_BILLING_RESET_CONTRACT_2,
            DeviceMessageId.DEVICE_ACTIONS_SET_PASSIVE_EOB_DATETIME
    );

    private final AbstractMessageExecutor messageExecutor;

    public EDPMessaging(AbstractMessageExecutor messageExecutor) {
        super(messageExecutor.getProtocol());
        this.messageExecutor = messageExecutor;
    }

    @Override
    public Set<DeviceMessageId> getSupportedMessages() {
        return supportedMessages;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        switch (propertySpec.getName()) {
            case activityCalendarCodeTableAttributeName:
                Code code = (Code) messageAttribute;
                EDPActivityCalendarParser parser = new EDPActivityCalendarParser(code);
                try {
                    parser.parse();
                } catch (IOException e) {
                    throw new GeneralParseException(MessageSeeds.GENERAL_PARSE_ERROR, e);
                }
                String dayProfile = ProtocolTools.getHexStringFromBytes(parser.getDayProfile().getBEREncodedByteArray(), "");
                String weekProfile = ProtocolTools.getHexStringFromBytes(parser.getWeekProfile().getBEREncodedByteArray(), "");
                String seasonProfile = ProtocolTools.getHexStringFromBytes(parser.getSeasonProfile().getBEREncodedByteArray(), "");
                return dayProfile + "|" + weekProfile + "|" + seasonProfile;
            case specialDaysCodeTableAttributeName:
                return parseSpecialDays((Code) messageAttribute);
            case configUserFileAttributeName:
            case firmwareUpdateFileAttributeName:
                FirmwareVersion firmwareVersion = ((FirmwareVersion) messageAttribute);
                return GenericMessaging.zipAndB64EncodeContent(firmwareVersion.getFirmwareFile());  //Bytes of the firmwareFile as string
            case activityCalendarActivationDateAttributeName:
                return String.valueOf(((Date) messageAttribute).getTime());     //Epoch
            default:
                return messageAttribute.toString();  //Used for String and BigDecimal attributes
        }
    }

    /**
     * Parse the special days of the given code table into the proper AXDR array.
     */
    private String parseSpecialDays(Code codeTable) {
        List<CodeCalendar> calendars = codeTable.getCalendars();
        Array result = new Array();
        int dayIndex = 1;
        for (CodeCalendar codeCalendar : calendars) {
            if (codeCalendar.getSeason() == 0) {
                byte[] timeStampBytes = {(byte) ((codeCalendar.getYear() == -1) ? 0xff : ((codeCalendar.getYear() >> 8) & 0xFF)), (byte) ((codeCalendar.getYear() == -1) ? 0xff : (codeCalendar.getYear()) & 0xFF),
                        (byte) ((codeCalendar.getMonth() == -1) ? 0xFF : codeCalendar.getMonth()), (byte) ((codeCalendar.getDay() == -1) ? 0xFF : codeCalendar.getDay()),
                        (byte) ((codeCalendar.getDayOfWeek() == -1) ? 0xFF : codeCalendar.getDayOfWeek())};
                OctetString timeStamp = OctetString.fromByteArray(timeStampBytes, timeStampBytes.length);
                Unsigned8 dayType = new Unsigned8(Integer.parseInt(codeCalendar.getDayType().getName()));
                Structure specialDayStructure = new Structure();
                specialDayStructure.addDataType(new Unsigned16(dayIndex));
                specialDayStructure.addDataType(timeStamp);
                specialDayStructure.addDataType(dayType);
                result.addDataType(specialDayStructure);
                dayIndex++;
            }
        }
        return ProtocolTools.getHexStringFromBytes(result.getBEREncodedByteArray(), "");
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return getMessageExecutor().updateSentMessages(sentMessages);
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getMessageExecutor().executePendingMessages(pendingMessages);
    }

    public AbstractMessageExecutor getMessageExecutor() {
        return messageExecutor;
    }
}
