/*
 * Copyright (c) 2023 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.dlms.itron.em620.messages;

import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.ResultType;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.cosem.Clock;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.GenericInvoke;
import com.energyict.dlms.cosem.ScriptTable;
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.ActivityCalendarController;
import com.energyict.protocolimpl.dlms.common.DLMSActivityCalendarController;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.ClockDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.PowerConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.CurrentRatioDenominatorAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.CurrentRatioNumeratorAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.VoltageRatioDenominatorAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.VoltageRatioNumeratorAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarCodeTableAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarNameAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.enableDSTAttributeName;

public class EM620MessageExecutor extends AbstractMessageExecutor {
    public static final String SEPARATOR = ";";
    private static final ObisCode BILLING_SCRIPT_TABLE_OBIS_CODE = ObisCode.fromString("0.0.10.0.1.255");

    private static final ObisCode CT_NUMERATOR = ObisCode.fromString("1.0.0.4.2.255");
    private static final ObisCode VT_NUMERATOR = ObisCode.fromString("1.0.0.4.3.255");
    private static final ObisCode CT_DENOMINATOR = ObisCode.fromString("1.0.0.4.5.255");
    private static final ObisCode VT_DENOMINATOR = ObisCode.fromString("1.0.0.4.6.255");
    private static final ObisCode START_MEASUREMENT_SCRIPT = ObisCode.fromString("0.0.10.128.108.255");

    private static final String WRITE_CT_VT_DISPLAY = "Write voltage and current ratios";

    private static final int CALENDAR_NAME_MAX_LENGTH = 8;
    private static final int START_MEASUREMENT_SCRIPT_METHOD = 1;
    private static final int START_MEASUREMENT_SCRIPT_METHOD_VALUE = 2;

    private static final int DATETIME_PART_NOT_DEFINED = 0XFF;
    private static final int DATETIME_NOT_SPECIFIED_DEVIATION = 0x80;
    private static final int DATETIME_LAST_DAY_OF_MONTH = 0xFD;
    private static final int DATETIME_SECOND_LAST_DAY_OF_MONTH = 0xFE;

    public EM620MessageExecutor(AbstractDlmsProtocol protocol) {
        super(protocol, protocol.getCollectedDataFactory(), protocol.getIssueFactory());
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = getCollectedDataFactory().createCollectedMessageList(pendingMessages);

        for (OfflineDeviceMessage pendingMessage : pendingMessages) {
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);   //Optimistic
            try {
                if (pendingMessage.getSpecification().equals(DeviceActionMessage.BILLING_RESET)) {
                    infoLog("Sending message BillingReset.");
                    doBillingReset();
                    infoLog("BillingReset message successful.");
                } else if (pendingMessage.getSpecification().equals(ClockDeviceMessage.EnableOrDisableDST)) {
                    infoLog("Sending EnableDST message.");
                    doEnableDST(pendingMessage);
                    infoLog("EnableDST message successful");
                } else if (pendingMessage.getSpecification().equals(ClockDeviceMessage.SetStartOfDST)) {
                    infoLog("Sending StartOfDST message.");
                    doSetDSTTime(pendingMessage, true);
                    infoLog("StartOfDST message successful");
                } else if (pendingMessage.getSpecification().equals(ClockDeviceMessage.SetEndOfDST)) {
                    infoLog("Sending EndOfDST message.");
                    doSetDSTTime(pendingMessage, false);
                    infoLog("EndOfDST message successful");
                } else if (pendingMessage.getSpecification().equals(PowerConfigurationDeviceMessage.SetVoltageAndCurrentParameters)) {
                    infoLog("Sending " + WRITE_CT_VT_DISPLAY + " message.");
                    setCtVt(pendingMessage);
                    infoLog(WRITE_CT_VT_DISPLAY + " message successful");
                } else if (pendingMessage.getSpecification().equals(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_FULL_CALENDAR_WITH_DATETIME)) {
                    activityCalendarWithActivationDate(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.ReadDLMSAttribute)) {
                    collectedMessage = this.readDlmsAttribute(collectedMessage, pendingMessage);
                } else {   //Unsupported message
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setFailureInformation(ResultType.NotSupported, createUnsupportedWarning(pendingMessage));
                    collectedMessage.setDeviceProtocolInformation("Message is currently not supported by the protocol");
                }
            } catch (IOException e) {
                if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getProtocol().getDlmsSessionProperties().getRetries() + 1)) {
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
                    collectedMessage.setDeviceProtocolInformation(e.getMessage());
                }
            }
            result.addCollectedMessage(collectedMessage);
        }
        return result;
    }

    private CollectedMessage readDlmsAttribute(CollectedMessage collectedMessage, OfflineDeviceMessage pendingMessage) {
        String obisCodeString = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.obisCode).getValue();
        int attributeId = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.attributeId).getValue());
        int classId = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.classId).getValue());

        obisCodeString = obisCodeString.replace(":", ".").replace("-", ".").replace(" ", "");
        ObisCode obisCode = ObisCode.fromString(obisCodeString);

        DLMSAttribute dlmsAttribute = new DLMSAttribute(obisCode, attributeId, classId);

        try {
            ComposedCosemObject composeObject = getCosemObjectFactory().getComposedCosemObject(dlmsAttribute);
            AbstractDataType abstractDataType = composeObject.getAttribute(dlmsAttribute);
            collectedMessage.setDeviceProtocolInformation(abstractDataType.toString());
        } catch (IOException e) {
            collectedMessage.setDeviceProtocolInformation(e.toString());
        }

        return collectedMessage;
    }

    private void doBillingReset() throws IOException {
        ScriptTable billingResetScriptTable = getCosemObjectFactory().getScriptTable(BILLING_SCRIPT_TABLE_OBIS_CODE);
        billingResetScriptTable.execute(1);
    }

    private void doEnableDST(OfflineDeviceMessage offlineDeviceMessage) throws IOException {
        int mode = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, enableDSTAttributeName).getValue());

        Clock clock = getProtocol().getDlmsSession().getCosemObjectFactory().getClock();
        clock.enableDisableDs(mode == 1);
    }

    protected void activityCalendarWithActivationDate(OfflineDeviceMessage pendingMessage) throws IOException {
        String calendarName = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, activityCalendarNameAttributeName).getValue();
        String epoch = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, activityCalendarActivationDateAttributeName).getValue();
        String activityCalendarContents = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, activityCalendarCodeTableAttributeName).getValue();
        if (calendarName.length() > CALENDAR_NAME_MAX_LENGTH) {
            calendarName = calendarName.substring(0, CALENDAR_NAME_MAX_LENGTH);
        }
        ActivityCalendarController activityCalendarController = new DLMSActivityCalendarController(getCosemObjectFactory(),
                getProtocol().getDlmsSession().getTimeZone(), false);
        activityCalendarController.parseContent(activityCalendarContents);
        activityCalendarController.writeCalendarName(calendarName);
        activityCalendarController.writeCalendar(); //Does not activate it yet
        Calendar activationCal = Calendar.getInstance(getProtocol().getTimeZone());
        activationCal.setTimeInMillis(Long.parseLong(epoch));
        activityCalendarController.writeCalendarActivationTime(activationCal);   //Activate now
    }

    private void doSetDSTTime(OfflineDeviceMessage offlineDeviceMessage, boolean startOfDST) throws IOException {
        int month = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.month).getValue());

        if (month < Calendar.JANUARY + 1 || month > Calendar.DECEMBER + 1) {
            throw new IOException("Failed to parse the message content. " + month + " is not a valid month. Message will fail.");
        }

        int dayOfMonth = DATETIME_PART_NOT_DEFINED;
        try {
            dayOfMonth = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.dayOfMonth).getValue());

            if (dayOfMonth == -1) {
                dayOfMonth = DATETIME_LAST_DAY_OF_MONTH;
            } else if (dayOfMonth == -2) {
                dayOfMonth = DATETIME_SECOND_LAST_DAY_OF_MONTH;
            } else if (isInvalidDayOfMonth(dayOfMonth)) {
                throw new IOException("Failed to parse the message content. " + dayOfMonth + " is not a valid Day of month. Message will fail.");
            }
        } catch (NumberFormatException e) {
            infoLog("Provided day of month is not valid, set it to not defined.");
        } // if parsing fails, just set value to initial state


        int dayOfWeek = DATETIME_PART_NOT_DEFINED;
        try {
            dayOfWeek = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.dayOfWeek).getValue());

            if (isInvalidDayOfWeek(dayOfWeek)) {
                throw new IOException("Failed to parse the message content. " + dayOfWeek + " is not a valid Day of week. Message will fail.");
            }
        } catch (NumberFormatException e) {
            infoLog("Provided day of week is not valid, set it to not defined.");
        } // if parsing fails, just set value to initial state

        int hour = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.hour).getValue());

        if (isInvalidHour(hour)) {
            throw new IOException("Failed to parse the message content. " + hour + " is not a valid hour. Message will fail.");
        }

        byte[] dsDateTimeByteArray = getDateTimeByteArray(month, dayOfMonth, dayOfWeek, hour);

        Clock clock = getProtocol().getDlmsSession().getCosemObjectFactory().getClock();
        if (startOfDST) {
            clock.setDsDateTimeBegin(new OctetString(dsDateTimeByteArray).getBEREncodedByteArray());
        } else {
            clock.setDsDateTimeEnd(new OctetString(dsDateTimeByteArray).getBEREncodedByteArray());
        }
    }

    private boolean isInvalidDayOfMonth(int dayOfMonth) {
        return dayOfMonth < 1 || dayOfMonth > 31;
    }

    private boolean isInvalidDayOfWeek(int dayOfWeek) {
        return dayOfWeek < 1 || dayOfWeek > 7;
    }

    private boolean isInvalidHour(int hour) {
        return hour < 0 || hour > 23;
    }

    private byte[] getDateTimeByteArray(int month, int dayOfMonth, int dayOfWeek, int hour) {
        return new byte[] {
                (byte) DATETIME_PART_NOT_DEFINED,
                (byte) DATETIME_PART_NOT_DEFINED,
                (byte) month,
                (byte) dayOfMonth,
                (byte) dayOfWeek,
                (byte) hour,
                (byte) 0x00,
                (byte) DATETIME_PART_NOT_DEFINED,
                (byte) DATETIME_PART_NOT_DEFINED,
                (byte) DATETIME_NOT_SPECIFIED_DEVIATION,
                0,
                (byte) DATETIME_PART_NOT_DEFINED
        };
    }

    private void setCtVt(OfflineDeviceMessage offlineDeviceMessage) throws IOException {
        int ct_numerator = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, CurrentRatioNumeratorAttributeName).getValue()).intValue();
        infoLog("Setting " + CurrentRatioNumeratorAttributeName + " to " + ct_numerator);
        getCosemObjectFactory().writeObject(CT_NUMERATOR, DLMSClassId.DATA.getClassId(),
                RegisterAttributes.VALUE.getAttributeNumber(), new Unsigned16(ct_numerator).getBEREncodedByteArray());
        infoLog("Done");

        int ct_denominator = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, CurrentRatioDenominatorAttributeName).getValue()).intValue();
        infoLog("Setting " + CurrentRatioDenominatorAttributeName + " to " + ct_denominator);
        getCosemObjectFactory().writeObject(CT_DENOMINATOR, DLMSClassId.DATA.getClassId(),
                RegisterAttributes.VALUE.getAttributeNumber(), new Unsigned16(ct_denominator).getBEREncodedByteArray());
        infoLog("Done");

        int vt_numerator = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, VoltageRatioNumeratorAttributeName).getValue()).intValue();
        infoLog("Setting " + VoltageRatioNumeratorAttributeName + " to " + vt_numerator);
        getCosemObjectFactory().writeObject(VT_NUMERATOR, DLMSClassId.DATA.getClassId(),
                RegisterAttributes.VALUE.getAttributeNumber(), new Unsigned16(vt_numerator).getBEREncodedByteArray());
        infoLog("Done");

        int vt_denominator = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, VoltageRatioDenominatorAttributeName).getValue()).intValue();
        infoLog("Setting " + VoltageRatioDenominatorAttributeName + " to " + vt_denominator);
        getCosemObjectFactory().writeObject(VT_DENOMINATOR, DLMSClassId.DATA.getClassId(),
                RegisterAttributes.VALUE.getAttributeNumber(), new Unsigned16(vt_denominator).getBEREncodedByteArray());
        infoLog("Done");

        infoLog("Executing start measurement script");
        GenericInvoke genericInvoke = getCosemObjectFactory().getGenericInvoke(START_MEASUREMENT_SCRIPT, DLMSClassId.SCRIPT_TABLE.getClassId(), START_MEASUREMENT_SCRIPT_METHOD);
        genericInvoke.invoke(new Unsigned16(START_MEASUREMENT_SCRIPT_METHOD_VALUE).getBEREncodedByteArray());
        infoLog("Done");
    }

    private void infoLog(String messageToLog) {
        getProtocol().getLogger().info(messageToLog);
    }
}
