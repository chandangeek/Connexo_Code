package com.energyict.protocolimpl.dlms.actarisace6000.messaging;

import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageElement;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageTagSpec;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;
import com.energyict.messaging.TimeOfUseMessageBuilder;
import com.energyict.protocol.MessageResult;
import com.energyict.protocolimpl.base.ActivityCalendarController;
import com.energyict.protocolimpl.dlms.actarisace6000.ACE6000;
import com.energyict.protocolimpl.dlms.common.DLMSActivityCalendarController;
import com.energyict.protocolimpl.messages.ProtocolMessageCategories;
import com.energyict.protocolimpl.messages.ProtocolMessages;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.messages.codetableparsing.CodeTableXmlParsing;
import com.energyict.protocolimpl.utils.ProtocolTools;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;

/**
 * ACE6000 Messages
 */
public class ACE6000Messages extends ProtocolMessages {
    private static final String SET_DISPLAY_MESSAGE = "Write a message to the LCD of the meter";
    public static final String RAW_CONTENT = "RawContent";
    private static final String SET_DISPLAY_MESSAGE_TAG = "SET_DISPLAY_MESSAGE";
    private static final String SELECTION_OF_12_LINES_IN_TOU_TABLE = RtuMessageConstant.SELECTION_OF_12_LINES_IN_TOU_TABLE;
    private static final String SELECTION_OF_12_LINES_IN_TOU_TABLE_DISPLAY = "Selection of 12 lines in TOU table display";
    private static final String BILLINGRESET = RtuMessageConstant.BILLINGRESET;
    private static final String BILLINGRESET_DISPLAY = "Billing reset";

    private final ACE6000 protocol;
    private final TariffCalendarExtractor tariffCalendarExtractor;
    private final TariffCalendarFinder tariffCalendarFinder;
    private final DeviceMessageFileFinder messageFileFinder;
    private final DeviceMessageFileExtractor deviceMessageFileExtractor;
    private int currentLineCalendarNumber = 0;

    public ACE6000Messages(ACE6000 ace6000, TariffCalendarExtractor tariffCalendarExtractor, TariffCalendarFinder tariffCalendarFinder, DeviceMessageFileFinder messageFileFinder, DeviceMessageFileExtractor deviceMessageFileExtractor) {
        this.protocol = ace6000;
        this.tariffCalendarExtractor = tariffCalendarExtractor;
        this.tariffCalendarFinder = tariffCalendarFinder;
        this.messageFileFinder = messageFileFinder;
        this.deviceMessageFileExtractor = deviceMessageFileExtractor;
    }

    @Override
    public void applyMessages(List messageEntries) throws IOException {

    }

    @Override
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        try {
            if (messageEntry.getContent() != null) {
                if (messageEntry.getContent().contains(RtuMessageConstant.TOU_ACTIVITY_CAL)) {
                    return writeActivityCalendar(messageEntry);
                } else if (messageEntry.getContent().contains(RtuMessageConstant.TOU_SPECIAL_DAYS)) {
                    return writeSpecialDays(messageEntry);
                } else if (messageEntry.getContent().contains(RtuMessageConstant.SELECTION_OF_12_LINES_IN_TOU_TABLE)) {
                    return write12LinesInActivityCalendar(messageEntry);
                } else if (messageEntry.getContent().contains(SET_DISPLAY_MESSAGE)) {
                    doWriteMessageToDisplay(getContentBetweenTags(messageEntry.getContent()));
                    return MessageResult.createSuccess(messageEntry);
                } else if (messageEntry.getContent().contains(BILLINGRESET)) {
                    infoLog("Sending message DemandReset.");
                    this.protocol.resetDemand();
                    infoLog("DemandReset message successful.");
                    return MessageResult.createSuccess(messageEntry);
                }
            } else if (isItThisMessage(messageEntry, TimeOfUseMessageBuilder.getMessageNodeTag())) {
                infoLog("Sending TimeOfUse message.");
                updateTimeOfUse(messageEntry);
                infoLog("TimeOfUse message successful");
                return MessageResult.createSuccess(messageEntry);
            } else {
                protocol.getLogger().log(Level.SEVERE, "Error executing message - the message content is empty, probably wrong file id specified.");
                return MessageResult.createFailed(messageEntry);
            }
        } catch (DataAccessResultException e) {
            protocol.getLogger().log(Level.SEVERE, "Error executing message: " + e.getMessage());
            return MessageResult.createFailed(messageEntry, e.getMessage());
        } catch (NumberFormatException e) {
            protocol.getLogger().log(Level.SEVERE, "Error executing message: " + e.getMessage());
            return MessageResult.createFailed(messageEntry, e.getMessage());
        } catch (SAXException e) {
            String msg = "Cannot process ActivityCalendar upgrade message due to an XML parsing error [" + e.getMessage() + "]";
            infoLog("Message failed - " + msg);
        }
        protocol.getLogger().log(Level.SEVERE, "Unexpected message: " + messageEntry.getContent());
        return MessageResult.createFailed(messageEntry);
    }

    /**
     * Checks if the given MessageEntry contains the corresponding MessageTag
     *
     * @param messageEntry the given messageEntry
     * @param messageTag   the tag to check
     * @return true if this is the message, false otherwise
     */
    protected boolean isItThisMessage(MessageEntry messageEntry, String messageTag) {
        return messageEntry.getContent().indexOf(messageTag) >= 0;
    }

    private MessageResult writeActivityCalendar(MessageEntry messageEntry) throws IOException {
        try {
            ActivityCalendarController activityCalendarController = new ACE6000ActivityCalendarController(protocol);
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

    private MessageResult write12LinesInActivityCalendar(MessageEntry messageEntry) throws IOException {
        try {
            ACE6000ActivityCalendarController activityCalendarController = new ACE6000ActivityCalendarController(protocol);
            activityCalendarController.parseContent(messageEntry.getContent());
            activityCalendarController.writeCalendarName("");
            if (currentLineCalendarNumber == 3) {
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

    @Override
    public List getMessageCategories() {
        List<MessageCategorySpec> categories = new ArrayList<MessageCategorySpec>();
        categories.add(ProtocolMessageCategories.getDemandResetCategory());
        MessageCategorySpec catDisplay = new MessageCategorySpec("'Display' Messages");
        catDisplay.addMessageSpec(addBasicMsg(SET_DISPLAY_MESSAGE, SET_DISPLAY_MESSAGE_TAG, false));
        categories.add(catDisplay);
        MessageCategorySpec catBilling = new MessageCategorySpec("Billing");
        MessageSpec msgSpec = addBasicMsg(BILLINGRESET_DISPLAY, BILLINGRESET, false);
        catBilling.addMessageSpec(msgSpec);
        categories.add(catBilling);
        MessageCategorySpec categorySelection12LinesTOU = new MessageCategorySpec(SELECTION_OF_12_LINES_IN_TOU_TABLE_DISPLAY);
        MessageSpec msgSpecSelection12LinesTOU = addBasicMsg(SELECTION_OF_12_LINES_IN_TOU_TABLE_DISPLAY, SELECTION_OF_12_LINES_IN_TOU_TABLE, false);
        catBilling.addMessageSpec(msgSpecSelection12LinesTOU);
        categories.add(categorySelection12LinesTOU);
        return categories;
    }

    private MessageSpec addBasicMsg(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    @Override
    public String writeMessage(Message msg) {
        return msg.write(this);
    }

    @Override
    public String writeTag(MessageTag msgTag) {
        if (msgTag.getName().equalsIgnoreCase(RtuMessageConstant.TOU_ACTIVITY_CAL) || msgTag.getName().equalsIgnoreCase(RtuMessageConstant.TOU_SPECIAL_DAYS)) {
            StringBuilder builder = new StringBuilder();

            // a. Opening tag
            builder.append("<");
            builder.append(msgTag.getName());
            builder.append(">");

            String name = "";
            String activationDate = "1";
            int codeId = 0;

            // b. Attributes
            for (Object o1 : msgTag.getAttributes()) {
                MessageAttribute att = (MessageAttribute) o1;
                if (RtuMessageConstant.TOU_ACTIVITY_NAME.equalsIgnoreCase(att.getSpec().getName())) {
                    if (att.getValue() != null) {
                        name = att.getValue();
                    }
                } else if (RtuMessageConstant.TOU_ACTIVITY_DATE.equalsIgnoreCase(att.getSpec().getName())) {
                    if (att.getValue() != null) {
                        activationDate = att.getValue();
                    }
                } else if (RtuMessageConstant.TOU_ACTIVITY_CODE_TABLE.equalsIgnoreCase(att.getSpec().getName())) {
                    if (att.getValue() != null) {
                        codeId = Integer.valueOf(att.getValue());
                    }
                }
            }

            Date actDate = new Date(Long.valueOf(activationDate));
            if (codeId > 0) {
                try {
                    String xmlContent = new CodeTableXmlParsing(tariffCalendarFinder, tariffCalendarExtractor).parseActivityCalendarAndSpecialDayTable(String.valueOf(codeId), Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime().before(actDate) ? actDate.getTime() : 1, name);
                    addChildTag(builder, RAW_CONTENT, ProtocolTools.compress(xmlContent));
                } catch (ParserConfigurationException e) {
                    protocol.getLogger().severe(e.getMessage());
                } catch (IOException e) {
                    protocol.getLogger().severe(e.getMessage());
                }
            }

            // d. Closing tag
            builder.append("</");
            builder.append(msgTag.getName());
            builder.append(">");
            return builder.toString();
        } else {
            StringBuilder buf = new StringBuilder();

            // a. Opening tag
            buf.append("<");
            buf.append(msgTag.getName());

            // b. Attributes
            for (Object o1 : msgTag.getAttributes()) {
                MessageAttribute att = (MessageAttribute) o1;
                if (att.getValue() == null || att.getValue().length() == 0) {
                    continue;
                }
                buf.append(" ").append(att.getSpec().getName());
                buf.append("=").append('"').append(att.getValue()).append('"');
            }
            buf.append(">");

            // c. sub elements
            for (Object o : msgTag.getSubElements()) {
                MessageElement elt = (MessageElement) o;
                if (elt.isTag()) {
                    buf.append(writeTag((MessageTag) elt));
                } else if (elt.isValue()) {
                    String value = writeValue((MessageValue) elt);
                    if (value == null || value.length() == 0) {
                        return "";
                    }
                    buf.append(value);
                }
            }

            // d. Closing tag
            buf.append("</");
            buf.append(msgTag.getName());
            buf.append(">");

            return buf.toString();
        }
    }

    /**
     * Adds a child tag to the given {@link StringBuffer}.
     *
     * @param buf     The string builder to whose contents the child tag needs to be added.
     * @param tagName The name of the child tag to add.
     * @param value   The contents (value) of the tag.
     */
    protected void addChildTag(StringBuilder buf, String tagName, Object value) {
        buf.append(System.getProperty("line.separator"));
        buf.append("<");
        buf.append(tagName);
        buf.append(">");
        buf.append(value);
        buf.append("</");
        buf.append(tagName);
        buf.append(">");
    }

    private void updateTimeOfUse(MessageEntry messageEntry) throws IOException, SAXException {
        final TimeOfUseMessageBuilder builder = new TimeOfUseMessageBuilder(messageFileFinder, deviceMessageFileExtractor);
        ActivityCalendarController activityCalendarController = new ACE6000ActivityCalendarController(this.protocol);
        builder.initFromXml(messageEntry.getContent());
        if (!builder.getCalendarId().isEmpty() && Integer.parseInt(builder.getCalendarId()) > 0) { // codeTable implementation
            infoLog("Parsing the content of the CodeTable.");
            activityCalendarController.parseContent(messageEntry.getContent());
            infoLog("Setting the new Passive Calendar Name.");
            activityCalendarController.writeCalendarName("");
            infoLog("Sending out the new Passive Calendar objects.");
            activityCalendarController.writeCalendar();
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
     *
     * @param message The message to show on the LCD of the device
     * @throws IOException
     */
    public void doWriteMessageToDisplay(String message) throws IOException {
        ACE6000DisplayController displayController = new ACE6000DisplayController(protocol);
        displayController.writeMessage(message);
    }

    private static String getContentBetweenTags(String value) {
        String returnValue = value;
        int startPos = returnValue.indexOf('>') + 1;
        int endPos = returnValue.lastIndexOf('<');
        return returnValue.substring(startPos, endPos);
    }

}
