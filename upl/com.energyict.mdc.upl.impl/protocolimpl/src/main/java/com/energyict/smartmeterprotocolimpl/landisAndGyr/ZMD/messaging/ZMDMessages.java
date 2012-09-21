package com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD.messaging;


import com.energyict.cbo.NestedIOException;
import com.energyict.dlms.axrdencoding.AxdrType;
import com.energyict.dlms.cosem.Clock;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.base.ActivityCalendarController;
import com.energyict.protocolimpl.messages.ProtocolMessageCategories;
import com.energyict.protocolimpl.messages.ProtocolMessages;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD.ZMD;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 19/01/12
 * Time: 13:05
 */
public class ZMDMessages extends ProtocolMessages implements TimeOfUseMessaging{

    public static String ENABLE_DST = "EnableDST";
    public static String START_OF_DST = "StartOfDST";
    public static String END_OF_DST = "EndOfDST";

    private final ZMD protocol;

    public ZMDMessages(final ZMD protocol) {
        this.protocol = protocol;
    }

    /**
     * Provides the full list of outstanding messages to the protocol.
     * If for any reason certain messages have to be grouped before they are sent to a device, then this is the place to do it.
     * At a later timestamp the framework will query each {@link com.energyict.protocol.MessageEntry} (see {@link #queryMessage(com.energyict.protocol.MessageEntry)}) to actually
     * perform the message.
     *
     * @param messageEntries a list of {@link com.energyict.protocol.MessageEntry}s
     * @throws java.io.IOException if a logical error occurs
     */
    public void applyMessages(final List messageEntries) throws IOException {
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
            } else {
                infoLog("Unknown message received.");
                return MessageResult.createUnknown(messageEntry);
            }
        } catch (IOException e) {
            infoLog("Message failed : " + e.getMessage());
        } catch (SAXException e) {
            String msg = "Cannot process ActivityCalendar upgrade message due to an XML parsing error [" + e.getMessage() + "]";
            infoLog("Message failed - " + msg);
        }
        return MessageResult.createFailed(messageEntry);
    }

    public List getMessageCategories() {
        List<MessageCategorySpec> categories = new ArrayList<MessageCategorySpec>();
        MessageCategorySpec catDaylightSaving = new MessageCategorySpec("Daylight saving");
        START_OF_DST = "StartOfDST";
        catDaylightSaving.addMessageSpec(addMsgWithValues("Program Start of Daylight Saving Time", START_OF_DST, false, false, "Month", "Day of month", "Day of week", "Hour"));
        END_OF_DST = "EndOfDST";
        catDaylightSaving.addMessageSpec(addMsgWithValues("Program End of Daylight Saving Time", END_OF_DST, false, false, "Month", "Day of month", "Day of week", "Hour"));
        catDaylightSaving.addMessageSpec(addMsgWithValue("Enable DST switch", ENABLE_DST, false, false));

        categories.add(ProtocolMessageCategories.getDemandResetCategory());
        categories.add(catDaylightSaving);
        return categories;
    }

    protected MessageSpec addMsgWithValue(final String description, final String tagName, final boolean advanced, boolean required) {
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
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" "); //Disable this field
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    private String getValueFromXMLAttribute(String tag, String content) throws IOException {
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
        final ZMDTimeOfUseMessageBuilder builder = new ZMDTimeOfUseMessageBuilder();
        ActivityCalendarController activityCalendarController = new ZMDActivityCalendarController((ZMD) this.protocol);
        builder.initFromXml(messageEntry.getContent());
        if (builder.getCodeId() > 0) { // codeTable implementation
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
            if (dayOfMonthString != null && dayOfMonthString.length() != 0) {
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
           if (dayOfWeekString != null && dayOfWeekString.length() != 0) {
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

        byte[] BEREncodedByteArray = new byte[]{
                AxdrType.OCTET_STRING.getTag(),
                (byte) 0x0c,
                (byte) 0xFF,
                (byte) 0xFF,
                (byte) month,
                (byte) dayOfMonth,
                (byte) dayOfWeek,
                (byte) hour,
                (byte) 0xFF,
                (byte) 0xFF,
                (byte) 0xFF,
                (byte) 0x80,
                0,
                (byte) (protocol.getDstFlag() == 1 ? 0x80 : 0x00)
        };

        Clock clock = protocol.getCosemObjectFactory().getClock();
        if (startOfDST) {
            clock.setDSTBeginDate(BEREncodedByteArray);
        } else {
            clock.setDSTEndDate(BEREncodedByteArray);
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
     * Returns the message builder capable of generating and parsing 'time of use' messages.
     *
     * @return The {@link com.energyict.protocol.messaging.MessageBuilder} capable of generating and parsing 'time of use' messages.
     */
    public TimeOfUseMessageBuilder getTimeOfUseMessageBuilder() {
        return new ZMDTimeOfUseMessageBuilder();
    }

    /**
     * Get the TimeOfUseMessagingConfig object that contains all the capabilities for the current protocol
     *
     * @return the config object
     */
    public TimeOfUseMessagingConfig getTimeOfUseMessagingConfig() {
        TimeOfUseMessagingConfig config = new TimeOfUseMessagingConfig();
        config.setNeedsName(true);
        config.setSupportsUserFiles(false);
        config.setSupportsCodeTables(true);
        config.setZipContent(true);
        return config;
    }
}