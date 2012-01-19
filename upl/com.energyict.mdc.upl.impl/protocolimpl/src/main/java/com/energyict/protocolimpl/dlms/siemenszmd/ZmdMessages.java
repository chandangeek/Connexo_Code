package com.energyict.protocolimpl.dlms.siemenszmd;

import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.base.ActivityCalendarController;
import com.energyict.protocolimpl.dlms.DLMSZMD;
import com.energyict.protocolimpl.messages.*;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 12-mei-2011
 * Time: 11:36:59
 */
public class ZmdMessages extends ProtocolMessages implements TimeOfUseMessaging{

    private final DLMSZMD protocol;

    public ZmdMessages(final DLMSZMD protocol) {
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
            } else {
                infoLog("Unknown message received.");
                return MessageResult.createUnknown(messageEntry);
            }
        } catch (IOException e){
            infoLog("Message failed : " + e.getMessage());
        }    catch (SAXException e) {
            String msg = "Cannot process ActivityCalendar upgrade message due to an XML parsing error [" + e.getMessage() + "]";
             infoLog("Message failed - Unable to parse the ActivityCalendar upgrade message:" + e.getMessage());
        }
        return MessageResult.createFailed(messageEntry);
    }

    public List getMessageCategories() {
        List<MessageCategorySpec> categories = new ArrayList<MessageCategorySpec>();
        categories.add(ProtocolMessageCategories.getDemandResetCategory());
        return categories;
    }

    private void updateTimeOfUse(MessageEntry messageEntry) throws IOException, SAXException {
        final ZMDTimeOfUseMessageBuilder builder = new ZMDTimeOfUseMessageBuilder();
        ActivityCalendarController activityCalendarController = new ZMDActivityCalendarController((DLMSZMD) this.protocol);
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
