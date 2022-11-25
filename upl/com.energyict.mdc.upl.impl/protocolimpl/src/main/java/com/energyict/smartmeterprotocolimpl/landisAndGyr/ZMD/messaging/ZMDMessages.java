package com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD.messaging;


import com.energyict.mdc.upl.io.NestedIOException;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.MessageAttributeSpec;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageTagSpec;
import com.energyict.mdc.upl.messages.legacy.MessageValueSpec;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.cosem.Clock;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.messaging.TimeOfUseMessageBuilder;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageResult;
import com.energyict.protocolimpl.base.ActivityCalendarController;
import com.energyict.protocolimpl.dlms.common.DLMSActivityCalendarController;
import com.energyict.protocolimpl.messages.ProtocolMessageCategories;
import com.energyict.protocolimpl.messages.ProtocolMessages;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD.ZMD;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.CurrentRatioNumeratorAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.VoltageRatioNumeratorAttributeName;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 19/01/12
 * Time: 13:05
 */
public class ZMDMessages extends ProtocolMessages {

    private static final ObisCode VTNumerator   = ObisCode.fromString("1.1.0.4.3.255");
    private static final ObisCode CTNumerator   = ObisCode.fromString("1.1.0.4.2.255");

    public static String ENABLE_DST = "EnableDST";
    public static String START_OF_DST = "StartOfDST";
    public static String END_OF_DST = "EndOfDST";
    private static final String SET_DISPLAY_MESSAGE = "SET_DISPLAY_MESSAGE";
    private static final String SET_DISPLAY_MESSAGE_TAG = "SET_DISPLAY_MESSAGE";
    private static final String BILLINGRESET = RtuMessageConstant.BILLINGRESET;
    private static final String BILLINGRESET_DISPLAY = "Billing reset";
    private static final String WRITE_VOLTAGE_AND_CURRENT_RATIOS = "Write voltage and current ratios";
    private static final String WRITE_VOLTAGE_NUMERATOR_DISPLAY = "Write voltage numerator";
    private static final String WRITE_CURRENT_NUMERATOR_DISPLAY = "Write current numerator";

    private final ZMD protocol;
    private final DeviceMessageFileFinder messageFileFinder;
    private final DeviceMessageFileExtractor messageFileExtractor;

    private int currentLineCalendarNumber = 0;

    public ZMDMessages(final ZMD protocol, DeviceMessageFileFinder messageFileFinder, DeviceMessageFileExtractor messageFileExtractor) {
        this.protocol = protocol;
        this.messageFileFinder = messageFileFinder;
        this.messageFileExtractor = messageFileExtractor;
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
    public void applyMessages(final List<MessageEntry> messageEntries) throws IOException {
        // Currently we don't do anything with the message
    }

    /**
     * Indicates that each message has to be executed by the protocol.
     *
     * @param messageEntry a definition of which message needs to be sent
     * @return a state of the message which was just sent
     * @throws java.io.IOException if a logical error occurs
     */
    public MessageResult queryMessage(final MessageEntry messageEntry) throws IOException {
        try {
            if (isItThisMessage(messageEntry, RtuMessageConstant.DEMAND_RESET)) {
                infoLog("Sending message DemandReset.");
                this.protocol.resetDemand();
                infoLog("DemandReset message successful.");
                return MessageResult.createSuccess(messageEntry);
            } else if (isItThisMessage(messageEntry, TimeOfUseMessageBuilder.getMessageNodeTag())) {
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
            } else if (isItThisMessage(messageEntry, RtuMessageConstant.TOU_ACTIVITY_CAL)) {
                return writeActivityCalendar(messageEntry);
            } else if (isItThisMessage(messageEntry, RtuMessageConstant.TOU_SPECIAL_DAYS)) {
                return writeSpecialDays(messageEntry);
            } else if (isItThisMessage(messageEntry, SET_DISPLAY_MESSAGE)) {
                doWriteMessageToDisplay(getContentBetweenTags(messageEntry.getContent()));
                return MessageResult.createSuccess(messageEntry);
            } else if (isItThisMessage(messageEntry, RtuMessageConstant.SELECTION_OF_12_LINES_IN_TOU_TABLE)) {
                return write12LinesInActivityCalendar(messageEntry);
            } else if (isItThisMessage(messageEntry, VoltageRatioNumeratorAttributeName)) {
                infoLog("Sending " + WRITE_VOLTAGE_NUMERATOR_DISPLAY + " message.");
                writeVoltageNumerator(getContentBetweenTags(messageEntry.getContent()));
                infoLog(WRITE_VOLTAGE_NUMERATOR_DISPLAY + " message successful");
                return MessageResult.createSuccess(messageEntry);
            } else if (isItThisMessage(messageEntry, CurrentRatioNumeratorAttributeName)) {
                infoLog("Sending " + WRITE_CURRENT_NUMERATOR_DISPLAY + " message.");
                writeCurrentNumerator(getContentBetweenTags(messageEntry.getContent()));
                infoLog(WRITE_CURRENT_NUMERATOR_DISPLAY + " message successful");
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
        } catch (IOException e) {
            infoLog("Message failed : " + e.getMessage());
        } catch (SAXException e) {
            String msg = "Cannot process ActivityCalendar upgrade message due to an XML parsing error [" + e.getMessage() + "]";
            infoLog("Message failed - " + msg);
        }
        return MessageResult.createFailed(messageEntry);
    }


    private MessageResult writeActivityCalendar(MessageEntry messageEntry) throws IOException {
        try {
            ActivityCalendarController activityCalendarController = new DLMSActivityCalendarController(protocol.getCosemObjectFactory(), protocol.getTimeZone());
            activityCalendarController.parseContent(messageEntry.getContent());
            activityCalendarController.writeCalendarName("");
            activityCalendarController.writeCalendar();
            protocol.getLogger().log(Level.INFO, "Activity calendar was successfully written");
            return MessageResult.createSuccess(messageEntry);
        } catch (DataAccessResultException e) {
            protocol.getLogger().severe("Writing of the activity calendar failed: " + e.getMessage());
            return MessageResult.createFailed(messageEntry, e.getMessage());   //Meter did not accept the specified code table, set message to failed
        }
    }

    private MessageResult write12LinesInActivityCalendar(MessageEntry messageEntry) throws IOException {
        try {
            ZMDActivityCalendarController activityCalendarController = new ZMDActivityCalendarController(protocol);
            activityCalendarController.parseContent(messageEntry.getContent());
            activityCalendarController.writeCalendarName("");
            if(currentLineCalendarNumber == 3){
                currentLineCalendarNumber = 0;
            }
            activityCalendarController.write12LinesCalendar(currentLineCalendarNumber);
            currentLineCalendarNumber++;
            protocol.getLogger().log(Level.INFO, "Activity calendar was successfully written");
            return MessageResult.createSuccess(messageEntry);
        } catch (DataAccessResultException e) {
            protocol.getLogger().severe("Writing of the activity calendar failed: " + e.getMessage());
            return MessageResult.createFailed(messageEntry, e.getMessage());   //Meter did not accept the specified code table, set message to failed
        }
    }

    private MessageResult writeSpecialDays(MessageEntry messageEntry) throws IOException {
        try {
            ActivityCalendarController activityCalendarController = new DLMSActivityCalendarController(protocol.getCosemObjectFactory(), protocol.getTimeZone());
            activityCalendarController.parseContent(messageEntry.getContent());
            activityCalendarController.writeSpecialDaysTable();
            protocol.getLogger().log(Level.INFO, "Special days were successfully written");
            return MessageResult.createSuccess(messageEntry);
        } catch (DataAccessResultException e) {
            protocol.getLogger().severe("Writing of the special days table failed: " + e.getMessage());
            return MessageResult.createFailed(messageEntry, e.getMessage());   //Meter did not accept the specified code table, set message to failed
        }
    }

    private void writeVoltageNumerator(String voltageNumerator) throws IOException {
        final Integer voltageNumeratorValue = Integer.valueOf( voltageNumerator );
        Data data = protocol.getCosemObjectFactory().getData(VTNumerator);
        data.setValueAttr(new Unsigned16( voltageNumeratorValue ));
    }

    private void writeCurrentNumerator(String currentNumerator) throws IOException {
        final Integer currentNumeratorValue = Integer.valueOf( currentNumerator );
        Data data = protocol.getCosemObjectFactory().getData(CTNumerator);
        data.setValueAttr(new Unsigned16( currentNumeratorValue ));
    }

    public List getMessageCategories() {
        List<MessageCategorySpec> categories = new ArrayList<MessageCategorySpec>();
        MessageCategorySpec catDaylightSaving = new MessageCategorySpec("Daylight saving");
        START_OF_DST = "StartOfDST";
        catDaylightSaving.addMessageSpec(addMsgWithValues("Program Start of Daylight Saving Time", START_OF_DST, false, false, "Month", "Day of month", "Day of week", "Hour"));
        END_OF_DST = "EndOfDST";
        catDaylightSaving.addMessageSpec(addMsgWithValues("Program End of Daylight Saving Time", END_OF_DST, false, false, "Month", "Day of month", "Day of week", "Hour"));
        catDaylightSaving.addMessageSpec(addMsgWithValue("Enable DST switch", ENABLE_DST, false));
        categories.add(catDaylightSaving);

        categories.add(ProtocolMessageCategories.getDemandResetCategory());

        MessageCategorySpec catDisplay = new MessageCategorySpec("'Display' Messages");
        catDisplay.addMessageSpec(addBasicMsg(SET_DISPLAY_MESSAGE, SET_DISPLAY_MESSAGE_TAG, false));
        categories.add(catDisplay);

        MessageCategorySpec catBilling = new MessageCategorySpec("Billing");
        MessageSpec  msgSpec = addBasicMsg(BILLINGRESET_DISPLAY, BILLINGRESET, false);
        catBilling.addMessageSpec(msgSpec);
        categories.add(catBilling);

        MessageCategorySpec voltageAndCurrentRatios = new MessageCategorySpec(WRITE_VOLTAGE_AND_CURRENT_RATIOS);
        MessageSpec writeVoltageNumerator = addBasicMsg(WRITE_VOLTAGE_NUMERATOR_DISPLAY, VoltageRatioNumeratorAttributeName, false);
        MessageSpec writeCurrentNumerator = addBasicMsg(WRITE_CURRENT_NUMERATOR_DISPLAY, CurrentRatioNumeratorAttributeName, false);
        voltageAndCurrentRatios.addMessageSpec( writeVoltageNumerator );
        voltageAndCurrentRatios.addMessageSpec( writeCurrentNumerator );
        categories.add(voltageAndCurrentRatios);

        return categories;
    }

    private static MessageSpec addBasicMsg(String messageType, String tag, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(messageType, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tag);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    protected MessageSpec addMsgWithValue(final String description, final String tagName, final boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(description, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageValueSpec msgVal = new MessageValueSpec();
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
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

    private void updateTimeOfUse(MessageEntry messageEntry) throws IOException, SAXException {
        final TimeOfUseMessageBuilder builder = new TimeOfUseMessageBuilder(this.messageFileFinder, this.messageFileExtractor);
        ActivityCalendarController activityCalendarController = new ZMDActivityCalendarController(this.protocol);
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

    /**
     * Procedure to enable/disable daylight switching
     * 1. execute this method to enable or disable the switching
     * 2. Adapt the device time zone, so it reflects the new situation (ex.: change timezone 'GMT+1' to 'Europe/Brussels').
     * 3. Issue a force sync clock - In some cases the device time is shifted 1 hour (occurs when summertime is active).
     */
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

        Clock clock = protocol.getCosemObjectFactory().getClock();
        clock.enableDisableDs(mode == 1 ? true : false);
    }

    private void setDSTTime(MessageEntry messageEntry, boolean startOfDST) throws IOException {
        int month, dayOfMonth, dayOfWeek, hour;

        try {
            String monthString = getValueFromXMLAttribute("Month", messageEntry.getContent());
            month = Integer.parseInt(monthString);
            if (month < 1 || month > 12) {
                throw new IOException("Failed to parse the message content. " + month + " is not a valid month. Message will fail.");
            }

            String dayOfMonthString = getValueFromXMLAttribute("Day of month", messageEntry.getContent());
            if (dayOfMonthString != null && !dayOfMonthString.isEmpty()) {
                dayOfMonth = Integer.parseInt(dayOfMonthString);
                if (dayOfMonth == -1) {
                    dayOfMonth = 0xFD;
                } else if (dayOfMonth == -2) {
                    dayOfMonth = 0xFE;
                } else if (dayOfMonth < -2 || dayOfMonth > 31) {
                    throw new IOException("Failed to parse the message content. " + dayOfMonth + " is not a valid Day of month. Message will fail.");
                }
            } else {
                dayOfMonth = 0xFF;
            }

            String dayOfWeekString = getValueFromXMLAttribute("Day of week", messageEntry.getContent());
           if (dayOfWeekString != null && !dayOfWeekString.isEmpty()) {
               dayOfWeek = Integer.parseInt(dayOfWeekString);
               if (dayOfWeek < 1 || dayOfWeek > 7) {
                   throw new IOException("Failed to parse the message content. " + dayOfWeek + " is not a valid Day of week. Message will fail.");
               }
           } else {
               dayOfWeek = 0xFF;
           }

            String hourString = getValueFromXMLAttribute("Hour", messageEntry.getContent());
            hour = Integer.parseInt(hourString);
            if (hour < 0 || hour > 23) {
                throw new IOException("Failed to parse the message content. " + hour + " is not a valid hour. Message will fail.");
            }
        } catch (NumberFormatException e) {
            infoLog("Failed to parse the message content. Message will fail.");
            throw new NestedIOException(e);
        }

        byte[] dsDateTimeByteArray = new byte[]{
                (byte) 0xFF,
                (byte) 0xFF,
                (byte) month,
                (byte) dayOfMonth,
                (byte) dayOfWeek,
                (byte) hour,
                (byte) 0x00,
                (byte) 0xFF,
                (byte) 0xFF,
                (byte) 0x80,
                0,
                (byte) 0xFF
        };

        Clock clock = protocol.getCosemObjectFactory().getClock();
        if (startOfDST) {
            clock.setDsDateTimeBegin(new OctetString(dsDateTimeByteArray).getBEREncodedByteArray());
        } else {
            clock.setDsDateTimeEnd(new OctetString(dsDateTimeByteArray).getBEREncodedByteArray());
        }
    }

    /**
     * Log the given message to the logger with the INFO level
     *
     * @param messageToLog
     */
    private void infoLog(String messageToLog) {
        this.protocol.getLogger().info(messageToLog);
    }

    /**
     * This command sends a message onto the display of the meter. This message
     * has the highest priority. This means that all other messages are
     * overwritten by this message in scrollmode.
     * @param message The message to show on the LCD of the device
     * @throws IOException
     */
    public void doWriteMessageToDisplay(String message) throws IOException {
       ZmdDisplayController displayController = new ZmdDisplayController(protocol);
        displayController.writeMessage(message);
    }

    private static String getContentBetweenTags(String value) {
        String returnValue = value;
        int startPos = returnValue.indexOf('>') + 1;
        int endPos = returnValue.lastIndexOf('<');
        return returnValue.substring(startPos, endPos);
    }

}
