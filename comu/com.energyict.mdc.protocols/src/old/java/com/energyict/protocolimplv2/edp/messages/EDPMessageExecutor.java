package com.energyict.protocolimplv2.edp.messages;

import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.protocols.util.TempFileLoader;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Integer16;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.util.AXDRDate;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.axrdencoding.util.AXDRTime;
import com.energyict.dlms.cosem.ActivityCalendar;
import com.energyict.dlms.cosem.DataAccessResultCode;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.cosem.ScriptTable;
import com.energyict.dlms.cosem.SpecialDaysTable;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;
import java.util.TimeZone;


/**
 * Copyrights EnergyICT
 * Date: 25/02/14
 * Time: 15:09
 * Author: khe
 */
public class EDPMessageExecutor extends AbstractMessageExecutor {

    public static final ObisCode RELAY_CONTROL_OBISCODE = ObisCode.fromString("0.0.96.3.10.255");
    public static final String SEPARATOR = ";";

    public EDPMessageExecutor(AbstractDlmsProtocol protocol, IssueService issueService, MdcReadingTypeUtilService readingTypeUtilService, CollectedDataFactory collectedDataFactory) {
        super(protocol, issueService, readingTypeUtilService, collectedDataFactory);
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = getCollectedDataFactory().createCollectedMessageList(pendingMessages);

        for (OfflineDeviceMessage pendingMessage : pendingMessages) {
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);   //Optimistic
            try {
                if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.CONTACTOR_CLOSE_RELAY)) {
                    closeRelay(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.CONTACTOR_OPEN_RELAY)) {
                    openRelay(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.CONTACTOR_SET_RELAY_CONTROL_MODE)) {
                    setRelayControlMode(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PUBLIC_LIGHTING_SET_RELAY_OPERATING_MODE)) {
                    setRelayOperatingMode(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PUBLIC_LIGHTING_SET_TIME_SWITCHING_TABLE)) {
                    setTimeSwitchingTable(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PUBLIC_LIGHTING_SET_THRESHOLD_OVER_CONSUMPTION)) {
                    setThresholdOverConsumption(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PUBLIC_LIGHTING_SET_OVERALL_MINIMUM_THRESHOLD)) {
                    setMinimumThreshold(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PUBLIC_LIGHTING_SET_OVERALL_MAXIMUM_THRESHOLD)) {
                    setMaximumThreshold(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PUBLIC_LIGHTING_SET_RELAY_TIME_OFFSETS_TABLE)) {
                    setTimeOffsetsTable(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE)) {
                    upgradeFirmware(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_CONTRACT)) {
                    writeActivityCalendar(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.ACTIVITY_CALENDER_SPECIAL_DAY_CALENDAR_SEND_WITH_CONTRACT_AND_DATETIME)) {
                    writeSpecialDays(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.DEVICE_ACTIONS_BILLING_RESET)) {
                    billingReset();
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.DEVICE_ACTIONS_BILLING_RESET_CONTRACT_1)) {
                    billingResetContract1();
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.DEVICE_ACTIONS_BILLING_RESET_CONTRACT_2)) {
                    billingResetContract2();
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.DEVICE_ACTIONS_SET_PASSIVE_EOB_DATETIME)) {
                    setPassiveEOBDateTime(pendingMessage);
                } else {   //Unsupported message
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setFailureInformation(ResultType.NotSupported, createUnsupportedWarning(pendingMessage));
                }
            } catch (IOException e) {
                if (IOExceptionHandler.isUnexpectedResponse(e, getProtocol().getDlmsSession())) {
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
                }   //Else: throw communication exception
            } catch (IndexOutOfBoundsException | ParseException | NumberFormatException e) {
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
            }
            result.addCollectedMessages(collectedMessage);
        }
        return result;
    }

    private void billingReset() throws IOException {
        getScriptTable().execute(7);
    }

    private void billingResetContract1() throws IOException {
        getScriptTable().execute(1);
    }

    private void billingResetContract2() throws IOException {
        getScriptTable().execute(2);
    }

    private ScriptTable getScriptTable() throws IOException {
        return getCosemObjectFactory().getScriptTable(ObisCode.fromByteArray(ScriptTable.LN_MDI_RESET));
    }

    private void setPassiveEOBDateTime(OfflineDeviceMessage pendingMessage) throws IOException {
        int contract = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.contractAttributeName).getDeviceMessageAttributeValue());
        String year = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.year).getDeviceMessageAttributeValue();
        String month = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.month).getDeviceMessageAttributeValue();
        String day = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.day).getDeviceMessageAttributeValue();
        String dayOfWeek = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.dayOfWeek).getDeviceMessageAttributeValue();
        String hour = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.hour).getDeviceMessageAttributeValue();
        String minute = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.minute).getDeviceMessageAttributeValue();
        String second = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.second).getDeviceMessageAttributeValue();

        StringBuilder hexDateTime = new StringBuilder();
        hexDateTime.append(parseDateFieldToHex(year, 2));
        hexDateTime.append(parseDateFieldToHex(month, 1));
        hexDateTime.append(parseDateFieldToHex(day, 1));
        hexDateTime.append(parseDateFieldToHex(dayOfWeek, 1));
        hexDateTime.append(parseDateFieldToHex(hour, 1));
        hexDateTime.append(parseDateFieldToHex(minute, 1));
        hexDateTime.append(parseDateFieldToHex(second, 1));
        hexDateTime.append("00800000");        //No hundredths, unspecified timezone

        byte[] dateTimeBytes = ProtocolTools.getBytesFromHexString(hexDateTime.toString(), "");

        ObisCode obisCode = ObisCode.fromString("0.0.94.35.0.255");
        if (contract == 1) {
            obisCode = ProtocolTools.setObisCodeField(obisCode, 4, (byte) 41);
        } else if (contract == 2) {
            obisCode = ProtocolTools.setObisCodeField(obisCode, 4, (byte) 42);
        } else {
            return;
        }
        getCosemObjectFactory().getData(obisCode).setValueAttr(OctetString.fromByteArray(dateTimeBytes, dateTimeBytes.length));
    }

    private String parseDateFieldToHex(String stringValue, int length) {
        int value;
        try {
            value = Integer.parseInt(stringValue);
        } catch (NumberFormatException e) {
            return stringValue;        //stringValue is already hex, e.g. FF or FE
        }
        return ProtocolTools.getHexStringFromInt(value, length, "");
    }

    private void writeSpecialDays(OfflineDeviceMessage pendingMessage) throws IOException {
        int contract = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.contractAttributeName).getDeviceMessageAttributeValue());
        String specialDaysHex = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.specialDaysAttributeName).getDeviceMessageAttributeValue();
        long activationDate = Long.parseLong(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.activityCalendarActivationDateAttributeName).getDeviceMessageAttributeValue());

        ObisCode obisCode;
        if (contract == 1) {
            obisCode = ObisCode.fromString("0.0.11.0.4.255");
        } else if (contract == 2) {
            obisCode = ObisCode.fromString("0.0.11.0.5.255");
        } else {
            return;
        }

        ObisCode activityCalendarObisCode;
        if (contract == 1) {
            activityCalendarObisCode = ObisCode.fromString("0.0.13.0.1.255");
        } else {
            activityCalendarObisCode = ObisCode.fromString("0.0.13.0.2.255");
        }

        Array specialDaysArray = new Array(ProtocolTools.getBytesFromHexString(specialDaysHex, ""), 0, 0);
        SpecialDaysTable specialDaysTable = getCosemObjectFactory().getSpecialDaysTable(obisCode);
        if (specialDaysArray.nrOfDataTypes() != 0) {
            specialDaysTable.writeSpecialDays(specialDaysArray);
        }

        AXDRDateTime axdrDateTime;
        axdrDateTime = convertUnixToDateTime(activationDate, getProtocol().getDlmsSession().getTimeZone());
        ActivityCalendar activityCalendar = getCosemObjectFactory().getActivityCalendar(activityCalendarObisCode);
        activityCalendar.writeActivatePassiveCalendarTime(new OctetString(axdrDateTime.getBEREncodedByteArray(), 0));
    }

    private void writeActivityCalendar(OfflineDeviceMessage pendingMessage) throws IOException {
        int contract = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.contractAttributeName).getDeviceMessageAttributeValue());
        String calendarName = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.activityCalendarNameAttributeName).getDeviceMessageAttributeValue();
        String profiles = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.activityCalendarAttributeName).getDeviceMessageAttributeValue();
        long epoch = Long.parseLong(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.activityCalendarActivationDateAttributeName).getDeviceMessageAttributeValue());

        ObisCode obisCode;
        if (contract == 1) {
            obisCode = ObisCode.fromString("0.0.13.0.1.255");
        } else if (contract == 2) {
            obisCode = ObisCode.fromString("0.0.13.0.2.255");
        } else {
            return;
        }
        String[] profilesSplit = profiles.split("\\|");
        String dayProfileHex = profilesSplit[0];
        String weekProfileHex = profilesSplit[1];
        String seasonProfileHex = profilesSplit[2];

        ActivityCalendar activityCalendar = getCosemObjectFactory().getActivityCalendar(obisCode);
        activityCalendar.writeDayProfileTablePassive(new Array(ProtocolTools.getBytesFromHexString(dayProfileHex, ""), 0, 0));
        activityCalendar.writeWeekProfileTablePassive(new Array(ProtocolTools.getBytesFromHexString(weekProfileHex, ""), 0, 0));
        activityCalendar.writeSeasonProfilePassive(new Array(ProtocolTools.getBytesFromHexString(seasonProfileHex, ""), 0, 0));
        activityCalendar.writeCalendarNamePassive(OctetString.fromString(calendarName));

        AXDRDateTime axdrDateTime = convertUnixToDateTime(epoch, getProtocol().getDlmsSession().getTimeZone());
        activityCalendar.writeActivatePassiveCalendarTime(new OctetString(axdrDateTime.getBEREncodedByteArray(), 0));
    }

    private AXDRDateTime convertUnixToDateTime(long epochInMillis, TimeZone timeZone) throws IOException {
        try {
            AXDRDateTime dateTime;
            Calendar cal = Calendar.getInstance(timeZone);
            cal.setTimeInMillis(epochInMillis);
            dateTime = new AXDRDateTime(cal.getTime(), timeZone);
            return dateTime;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw new IOException("Could not parse " + epochInMillis + " to a long value");
        }
    }

    private void closeRelay(OfflineDeviceMessage pendingMessage) throws IOException {
        int relayNumber = Integer.valueOf(pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
        ObisCode obisCode = ProtocolTools.setObisCodeField(RELAY_CONTROL_OBISCODE, 1, (byte) relayNumber);
        getCosemObjectFactory().getDisconnector(obisCode).remoteReconnect();
    }

    private void openRelay(OfflineDeviceMessage pendingMessage) throws IOException {
        int relayNumber = Integer.valueOf(pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
        ObisCode obisCode = ProtocolTools.setObisCodeField(RELAY_CONTROL_OBISCODE, 1, (byte) relayNumber);
        getCosemObjectFactory().getDisconnector(obisCode).remoteDisconnect();
    }

    private void setThresholdOverConsumption(OfflineDeviceMessage pendingMessage) throws IOException, ParseException {
        int threshold = Integer.valueOf(pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
        ObisCode obisCode = ObisCode.fromString("0.1.94.35.44.255");
        getCosemObjectFactory().getRegister(obisCode).setValueAttr(new Unsigned32(threshold));
    }

    private void setMinimumThreshold(OfflineDeviceMessage pendingMessage) throws IOException, ParseException {
        int threshold = Integer.valueOf(pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
        ObisCode obisCode = ObisCode.fromString("0.1.94.35.45.255");
        getCosemObjectFactory().getRegister(obisCode).setValueAttr(new Unsigned32(threshold));
    }

    private void upgradeFirmware(OfflineDeviceMessage pendingMessage) throws IOException, ParseException {
        String path = pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue();
        String base64encodedImage = new String(TempFileLoader.loadTempFile(path));
        byte[] binaryImage = Base64.getDecoder().decode(base64encodedImage);

        ImageTransfer imageTransfer = getCosemObjectFactory().getImageTransfer();
        imageTransfer.setBooleanValue(0x01);    //Meter only takes 0x01 as boolean value "true"
        imageTransfer.setUsePollingVerifyAndActivate(true);         //Use polling to check the result of the image verification
        imageTransfer.upgrade(binaryImage, false);
        imageTransfer.setUsePollingVerifyAndActivate(false);    //Don't use polling for the activation, the meter reboots immediately!

        try {
            imageTransfer.imageActivation();
        } catch (IOException e) {
            if (isTemporaryFailure(e) || isTemporaryFailure(e.getCause()) || isHardwareFault(e) || isHardwareFault(e.getCause())) {
                //Move on in case of temporary failure/hardware fault,
                return;
            } else {
                throw e;
            }
        }
    }

    private boolean isTemporaryFailure(Throwable e) {
        if (e == null) {
            return false;
        } else if (e instanceof DataAccessResultException) {
            return (((DataAccessResultException) e).getDataAccessResult() == DataAccessResultCode.TEMPORARY_FAILURE.getResultCode());
        } else {
            return false;
        }
    }

    private boolean isHardwareFault(Throwable e) {
        if (e == null) {
            return false;
        } else if (e instanceof DataAccessResultException) {
            return ((DataAccessResultException) e).getDataAccessResult() == DataAccessResultCode.HARDWARE_FAULT.getResultCode();
        } else {
            return false;
        }
    }

    private void setTimeOffsetsTable(OfflineDeviceMessage pendingMessage) throws IOException, ParseException {
        int relayNumber = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.relayNumberAttributeName).getDeviceMessageAttributeValue());
        String[] beginDates = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.beginDatesAttributeName).getDeviceMessageAttributeValue().split(SEPARATOR);
        String[] endDates = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.endDatesAttributeName).getDeviceMessageAttributeValue().split(SEPARATOR);
        String[] offOffsets = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.offOffsetsAttributeName).getDeviceMessageAttributeValue().split(SEPARATOR);
        String[] onOffsets = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.onOffsetsAttributeName).getDeviceMessageAttributeValue().split(SEPARATOR);

        ObisCode obisCode;
        if (relayNumber == 1) {
            obisCode = ObisCode.fromString("0.1.94.35.48.255");
        } else if (relayNumber == 2) {
            obisCode = ObisCode.fromString("0.1.94.35.148.255");
        } else {
            return;
        }

        Array array = new Array();
        for (int index = 0; index < beginDates.length; index++) {
            Structure structure = new Structure();
            structure.addDataType(parseDateFromString(beginDates[index]));
            structure.addDataType(parseDateFromString(endDates[index]));
            structure.addDataType(new Integer16(Integer.parseInt(offOffsets[index])));
            structure.addDataType(new Integer16(Integer.parseInt(onOffsets[index])));
            array.addDataType(structure);
        }
        getCosemObjectFactory().getData(obisCode).setValueAttr(array);
    }

    private void setMaximumThreshold(OfflineDeviceMessage pendingMessage) throws IOException, ParseException {
        int threshold = Integer.valueOf(pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
        ObisCode obisCode = ObisCode.fromString("0.1.94.35.46.255");
        getCosemObjectFactory().getRegister(obisCode).setValueAttr(new Unsigned32(threshold));
    }

    private void setTimeSwitchingTable(OfflineDeviceMessage pendingMessage) throws IOException, ParseException {
        int relayNumber = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.relayNumberAttributeName).getDeviceMessageAttributeValue());
        String userFileContent = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.configUserFileAttributeName).getDeviceMessageAttributeValue();

        ObisCode obisCode;
        if (relayNumber == 1) {
            obisCode = ObisCode.fromString("0.1.94.35.41.255");
        } else if (relayNumber == 2) {
            obisCode = ObisCode.fromString("0.1.94.35.141.255");
        } else {
            return;
        }

        List<String> entries = new ArrayList<>();
        try (Scanner scanner = new Scanner(userFileContent)) {
            while (scanner.hasNextLine()) {
                entries.add(scanner.nextLine());
            }
        }

        Array array = new Array();
        Structure structure;
        for (String entry : entries) {
            structure = new Structure();
            String[] split = entry.split(SEPARATOR);
            if (split.length == 4) {
                structure.addDataType(parseDateFromString(split[0]));
                structure.addDataType(parseDateFromString(split[1]));
                structure.addDataType(parseTimeFromString(split[2]));
                structure.addDataType(parseTimeFromString(split[3]));
            }
            array.addDataType(structure);
        }
        getCosemObjectFactory().getData(obisCode).setValueAttr(array);
    }

    /**
     * String format should be yyyyMMdd
     */
    private OctetString parseDateFromString(String switchOffTime) throws ParseException {
        return AXDRDate.fromDate(switchOffTime, getProtocol().getDlmsSession().getTimeZone());
    }

    /**
     * String format should be hh:mm:ss
     */
    private OctetString parseTimeFromString(String dateString) throws ParseException, IOException {
        AXDRTime axdrTime = new AXDRTime();
        axdrTime.setTime(dateString);
        return axdrTime.getOctetString();
    }

    private void setRelayControlMode(OfflineDeviceMessage pendingMessage) throws IOException {
        int relayNumber = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.relayNumberAttributeName).getDeviceMessageAttributeValue());
        int controlMode = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.contactorModeAttributeName).getDeviceMessageAttributeValue());
        ObisCode obisCode = ProtocolTools.setObisCodeField(RELAY_CONTROL_OBISCODE, 1, (byte) relayNumber);
        getCosemObjectFactory().getDisconnector(obisCode).writeControlMode(new TypeEnum(controlMode));
    }

    private void setRelayOperatingMode(OfflineDeviceMessage pendingMessage) throws IOException {
        int relayNumber = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.relayNumberAttributeName).getDeviceMessageAttributeValue());
        int operatingMode = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.relayOperatingModeAttributeName).getDeviceMessageAttributeValue());

        ObisCode obisCode;
        if (relayNumber == 1) {
            obisCode = ObisCode.fromString("0.1.94.35.40.255");
        } else if (relayNumber == 2) {
            obisCode = ObisCode.fromString("0.1.94.35.140.255");
        } else {
            return;
        }
        getCosemObjectFactory().getData(obisCode).setValueAttr(new TypeEnum(operatingMode));
    }
}