package com.energyict.smartmeterprotocolimpl.actaris.sl7000.messaging;

import com.energyict.mdc.io.NestedIOException;
import com.energyict.mdc.upl.messages.legacy.DateFormatter;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.MessageAttributeSpec;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageTagSpec;
import com.energyict.mdc.upl.messages.legacy.MessageValueSpec;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.ScriptTable;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageResult;
import com.energyict.protocolimpl.base.ActivityCalendarController;
import com.energyict.protocolimpl.messages.ProtocolMessageCategories;
import com.energyict.protocolimpl.messages.ProtocolMessages;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.actaris.sl7000.ActarisSl7000;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 23/04/12
 * Time: 11:59
 */
public class Messages extends ProtocolMessages {

    public static String ENABLE_DST = "EnableDST";
    public static String START_OF_DST = "StartOfDST";
    public static String END_OF_DST = "EndOfDST";
    public static String BATTERY_EXPIRY = "BatteryExpiry";

    public static ObisCode DST_WORKING_MODE_OBIS = ObisCode.fromString("0.0.131.0.4.255");
    public static ObisCode DST_GENERIC_PARAMS_OBIS = ObisCode.fromString("0.0.131.0.6.255");
    public static ObisCode BATTERY_EXPIRY_OBIS = ObisCode.fromString("0.0.96.6.2.255");
    public static ObisCode BILLING_RESET_OBIS = ObisCode.fromString("0.0.10.0.1.255");

    private final ActarisSl7000 protocol;
    private final TariffCalendarFinder calendarFinder;
    private final TariffCalendarExtractor tariffCalendarExtractor;
    private final DeviceMessageFileFinder messageFileFinder;
    private final DeviceMessageFileExtractor deviceMessageFileExtractor;
    private final DateFormatter dateFormatter;

    public Messages(final ActarisSl7000 protocol, TariffCalendarFinder calendarFinder, TariffCalendarExtractor tariffCalendarExtractor, DeviceMessageFileFinder messageFileFinder, DeviceMessageFileExtractor deviceMessageFileExtractor, DateFormatter dateFormatter) {
        this.protocol = protocol;
        this.calendarFinder = calendarFinder;
        this.tariffCalendarExtractor = tariffCalendarExtractor;
        this.deviceMessageFileExtractor = deviceMessageFileExtractor;
        this.messageFileFinder = messageFileFinder;
        this.dateFormatter = dateFormatter;
    }

    /**
     * Provides the full list of outstanding messages to the protocol.
     * If for any reason certain messages have to be grouped before they are sent to a device, then this is the place to do it.
     * At a later timestamp the framework will query each {@link MessageEntry} (see {@link #queryMessage(MessageEntry)}) to actually
     * perform the message.
     *
     * @param messageEntries a list of {@link MessageEntry}s
     * @throws java.io.IOException if a logical error occurs
     */
    public void applyMessages(final List messageEntries) throws IOException {
        // Nothing to implement
    }

    /**
     * Indicates that each message has to be executed by the protocol.
     *
     * @param messageEntry a definition of which message needs to be sent
     * @return a state of the message which was just sent
     * @throws java.io.IOException if a logical error occurs
     */
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
         try {
            if (isItThisMessage(messageEntry, TimeOfUseMessageBuilder.getMessageNodeTag())) {
                infoLog("Sending TimeOfUse message.");
                updateTimeOfUse(messageEntry);
                infoLog("TimeOfUse message successful");
                return MessageResult.createSuccess(messageEntry);
            } else if (isItThisMessage(messageEntry, ENABLE_DST)) {
                infoLog("Sending EnableDST message.");
                enableDST(messageEntry);
                infoLog("EnableDST message successful");
                return MessageResult.createSuccess(messageEntry);
            } else if (isItThisMessage(messageEntry, START_OF_DST)) {
                infoLog("Sending StartOfDST message.");
                setDSTTime(messageEntry, true);
                infoLog("StartOfDST message successful");
                return MessageResult.createSuccess(messageEntry);
            } else if (isItThisMessage(messageEntry, END_OF_DST)) {
                infoLog("Sending EndOfDST message.");
                setDSTTime(messageEntry, false);
                infoLog("EndOfDST message successful");
                return MessageResult.createSuccess(messageEntry);
            } else if ((isItThisMessage(messageEntry,  BATTERY_EXPIRY))) {
                infoLog("Programming new battery expiry date.");
                programBatteryExpiryDate(messageEntry);
                infoLog("New battery expiry date successful programmed.");
                return MessageResult.createSuccess(messageEntry);
            } else if ((isItThisMessage(messageEntry, RtuMessageConstant.DEMAND_RESET))) {
                infoLog("Sending message DemandReset.");
                doBillingReset();
                infoLog("DemandReset message successful.");
                return MessageResult.createSuccess(messageEntry);
        } else {
                infoLog("Unknown message received.");
                return MessageResult.createUnknown(messageEntry);
            }
         } catch (NestedIOException e) {
             if (ProtocolTools.getRootCause(e) instanceof ConnectionException || ProtocolTools.getRootCause(e) instanceof DLMSConnectionException) {
                 throw e;   // In case of a connection exception (of which we cannot recover), do throw the error.
             }
             infoLog("Message failed : " + e.getMessage());
        } catch (IOException e){
            infoLog("Message failed : " + e.getMessage());
        }    catch (SAXException e) {
             infoLog("Message failed - Cannot process ActivityCalendar upgrade message due to an XML parsing error [" + e.getMessage() + "]");
        }
        return MessageResult.createFailed(messageEntry);
    }

    public List<MessageCategorySpec> getMessageCategories() {
        List<MessageCategorySpec> categories = new ArrayList<>();
        MessageCategorySpec catDst = new MessageCategorySpec("Daylight saving");
        catDst.addMessageSpec(addMsgWithValues("Program Start of Daylight Saving Time", START_OF_DST, false, false, "Month", "Day of month", "Day of week", "Hour"));
        catDst.addMessageSpec(addMsgWithValues("Program End of Daylight Saving Time", END_OF_DST, false, false, "Month", "Day of month", "Day of week", "Hour"));
        catDst.addMessageSpec(addMsgWithValue("Enable DST switch", ENABLE_DST, false, false));
        categories.add(catDst);

        MessageCategorySpec catBattery = new MessageCategorySpec("Battery");
        catBattery.addMessageSpec(addMsgWithValues("Program Battery expiry date", BATTERY_EXPIRY, false, true, "Date (dd/MM/yyyy)"));
        categories.add(catBattery);

        categories.add(ProtocolMessageCategories.getDemandResetCategory());
        return categories;
    }

    protected MessageSpec addMsgWithValues(final String description, final String tagName, final boolean advanced, boolean required, String... attr) {
        MessageSpec msgSpec = new MessageSpec(description, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        for (String attribute : attr) {
            tagSpec.add(new MessageAttributeSpec(attribute, required));
        }
        tagSpec.add(new MessageValueSpec(" "));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    protected MessageSpec addMsgWithValue(final String description, final String tagName, final boolean advanced, boolean required) {
        MessageSpec msgSpec = new MessageSpec(description, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageValueSpec msgVal = new MessageValueSpec();
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    private String getValueFromXMLAttribute(String tag, String content) {
        int startIndex = content.indexOf(tag + "=\"");
        if (startIndex != -1) {
            int endIndex = content.indexOf("\"", startIndex + tag.length() + 2);
            try {
                return content.substring(startIndex + tag.length() + 2, endIndex);
            } catch (IndexOutOfBoundsException e) {
            }
        }
        return "";
    }

    private String getValueFromXML(String tag, String content) {
        int startIndex = content.indexOf("<" + tag);
        int endIndex = content.indexOf("</" + tag);
        return content.substring(startIndex + tag.length() + 2, endIndex);
    }

    /**
     * Log the given message to the logger with the INFO level
     *
     * @param messageToLog
     */
    private void infoLog(String messageToLog) {
        this.protocol.getLogger().info(messageToLog);
    }

    private void warningLog(String messageToLog) {
        this.protocol.getLogger().warning(messageToLog);
    }

    private void updateTimeOfUse(MessageEntry messageEntry) throws IOException, SAXException {
        final TimeOfUseMessageBuilder builder = new TimeOfUseMessageBuilder(this.calendarFinder, this.messageFileFinder, this.dateFormatter, this.deviceMessageFileExtractor, this.tariffCalendarExtractor);
        ActivityCalendarController activityCalendarController = new com.energyict.smartmeterprotocolimpl.actaris.sl7000.messaging.ActivityCalendarController(this.protocol);
        builder.initFromXml(messageEntry.getContent());
        if (!builder.getCalendarId().isEmpty()) { // codeTable implementation
            infoLog("Parsing the content of the CodeTable.");
            activityCalendarController.parseContent(messageEntry.getContent());
            infoLog("Setting the new Passive Calendar Name.");
            activityCalendarController.writeCalendarName("");
            infoLog("Sending out the new Passive Calendar objects.");
            activityCalendarController.writeCalendar();
        }
    }

    private void enableDST(MessageEntry entry) throws IOException {
        int mode = Integer.parseInt(getValueFromXML(ENABLE_DST, entry.getContent()));
        if (mode == 0) {
            infoLog("Disabling DST switching.");
        } else if (mode == 1) {
            infoLog("Enabling DST switching");
        } else {
            String messageToLog = "Failed to parse the message value.";
            infoLog(messageToLog);
            throw new IOException(messageToLog);
        }

        Data data = protocol.getDlmsSession().getCosemObjectFactory().getData(DST_WORKING_MODE_OBIS);
        Unsigned8 newMode = new Unsigned8(mode);
        data.setValueAttr(newMode);
    }

    private void setDSTTime(MessageEntry messageEntry, boolean startOfDST) throws IOException {
        int month, dayOfMonth, dayOfWeek, hour;
        try {
            String monthString = getValueFromXMLAttribute("Month", messageEntry.getContent());
            month = (monthString != null && monthString.length() != 0) ? Integer.parseInt(monthString) : 0x7F;

            String dayOfMonthString = getValueFromXMLAttribute("Day of month", messageEntry.getContent());
            dayOfMonth = (dayOfMonthString != null && dayOfMonthString.length() != 0) ? Integer.parseInt(dayOfMonthString) : 0x7F;

            String dayOfWeekString = getValueFromXMLAttribute("Day of week", messageEntry.getContent());
            dayOfWeek = (dayOfWeekString != null && dayOfWeekString.length() != 0) ? Integer.parseInt(dayOfWeekString) : 0x7F;
            if (dayOfWeek == 0x07) {
                dayOfWeek = 0x00;       // Sunday is day 0, not 7;
            }

            String hourString = getValueFromXMLAttribute("Hour", messageEntry.getContent());
            hour = Integer.parseInt(hourString);

        } catch (NumberFormatException e) {
            infoLog("Error: Failed to parse the message content. Message will fail.");
            throw new NestedIOException(e);
        }

        checkSetDSTWorkingMode();
        Data genericDSTParameters = protocol.getDlmsSession().getCosemObjectFactory().getData(DST_GENERIC_PARAMS_OBIS);
        Structure oldStructure = (Structure) genericDSTParameters.getValueAttr();

        OctetString dateAndTime = new OctetString(new byte[]{
                (byte) 0x7F,
                (byte) month,
                (byte) dayOfMonth,
                (byte) dayOfWeek,
                (byte) hour
        });
        Structure newStructure = new Structure();

        Structure dateAndTimeStruct = new Structure();
        dateAndTimeStruct.addDataType(dateAndTime);
        dateAndTimeStruct.addDataType(new Unsigned8(startOfDST ? 0 : 1));
        Structure innerStruct = new Structure();
        innerStruct.addDataType(new Unsigned8(33));
        innerStruct.addDataType(new Unsigned8(0));
        dateAndTimeStruct.addDataType(innerStruct);

        Array datesArray = new Array();
        if (startOfDST) {
            datesArray.addDataType(dateAndTimeStruct);
            Structure structure = ((Array) oldStructure.getStructure().getDataType(1)).getDataType(1).getStructure();
            datesArray.addDataType(structure);
        } else {
            datesArray.addDataType(((Array)oldStructure.getStructure().getDataType(1)).getDataType(0).getStructure());
            datesArray.addDataType(dateAndTimeStruct);
        }

        newStructure.addDataType(new Unsigned8(60));
        newStructure.addDataType(datesArray);

        genericDSTParameters.setValueAttr(newStructure);
    }

    private void checkSetDSTWorkingMode() throws IOException {
        Data data = protocol.getDlmsSession().getCosemObjectFactory().getData(DST_WORKING_MODE_OBIS);
        long mode = data.getValue();
        if (mode != 1) {
            if (mode == 0) {
                warningLog("Warning: DST switching is not enabled - Please enable switching by using 'Enable Daylight Saving Time' message.");
            } else {
                // Incompatible DST working mode found - set the mode to 1.
                infoLog("Incompatible DST working mode (" + mode + "). The mode will be set to 1.");
                Unsigned8 newMode = new Unsigned8(1);
                data.setValueAttr(newMode);
            }
        }
    }

    private void programBatteryExpiryDate(MessageEntry messageEntry) throws IOException {
        int year, month, dayOfMonth;
        try {
            String dateString = getValueFromXMLAttribute("Date (dd/MM/yyyy)", messageEntry.getContent());
            String[] parts = dateString.split("/");
            dayOfMonth = Integer.parseInt(parts[0]);
            month = Integer.parseInt(parts[1]);
            year = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            infoLog("Error: Failed to parse the message content. Message will fail.");
            throw new NestedIOException(e);
        }

        Data expiryObject = protocol.getDlmsSession().getCosemObjectFactory().getData(BATTERY_EXPIRY_OBIS);

        byte[] expiryDate = new byte[12];
        expiryDate[0] = (byte) ((year & 0xFF00) >> 8);
        expiryDate[1] = (byte) (year & 0xFF);
        expiryDate[2] = (byte) month;
        expiryDate[3] = (byte) dayOfMonth;
        for (int i = 4; i < 12; i++) {
            expiryDate[i] = (byte) 0xFF;
        }
        OctetString data = new OctetString(expiryDate);
        expiryObject.setValueAttr(data);
    }

    private void doBillingReset() throws IOException {
        ScriptTable billingResetScriptTable = protocol.getDlmsSession().getCosemObjectFactory().getScriptTable(BILLING_RESET_OBIS);
        billingResetScriptTable.execute(1);
    }
}