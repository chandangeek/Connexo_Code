package com.energyict.protocolimpl.dlms.siemenszmd;

import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.Formatter;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;

import com.energyict.messaging.TimeOfUseMessageBuilder;
import com.energyict.protocol.MessageResult;
import com.energyict.protocolimpl.base.ActivityCalendarController;
import com.energyict.protocolimpl.dlms.DLMSZMD;
import com.energyict.protocolimpl.messages.ProtocolMessageCategories;
import com.energyict.protocolimpl.messages.ProtocolMessages;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 12-mei-2011
 * Time: 11:36:59
 */
public class ZmdMessages extends ProtocolMessages {

    private final DLMSZMD protocol;
    private final TariffCalendarFinder calendarFinder;
    private final TariffCalendarExtractor tariffCalendarExtractor;
    private final DeviceMessageFileExtractor deviceMessageFileExtractor;
    private final DeviceMessageFileFinder messageFileFinder;
    private final Formatter formatter;

    public ZmdMessages(final DLMSZMD protocol, TariffCalendarFinder calendarFinder, TariffCalendarExtractor tariffCalendarExtractor, DeviceMessageFileFinder messageFileFinder, DeviceMessageFileExtractor deviceMessageFileExtractor, Formatter formatter) {
        this.protocol = protocol;
        this.calendarFinder = calendarFinder;
        this.tariffCalendarExtractor = tariffCalendarExtractor;
        this.deviceMessageFileExtractor = deviceMessageFileExtractor;
        this.messageFileFinder = messageFileFinder;
        this.formatter = formatter;
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
        } catch (IOException e) {
            infoLog("Message failed : " + e.getMessage());
        } catch (SAXException e) {
            infoLog("Message failed - Unable to parse the ActivityCalendar upgrade message:" + e.getMessage());
        }
        return MessageResult.createFailed(messageEntry);
    }

    public List<MessageCategorySpec> getMessageCategories() {
        return Collections.singletonList(ProtocolMessageCategories.getDemandResetCategory());
    }

    private void updateTimeOfUse(MessageEntry messageEntry) throws IOException, SAXException {
        final ZMDTimeOfUseMessageBuilder builder = new ZMDTimeOfUseMessageBuilder(this.calendarFinder, this.tariffCalendarExtractor, this.messageFileFinder, this.deviceMessageFileExtractor, this.formatter);
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
     * Log the given message to the logger with the INFO level
     *
     * @param messageToLog
     */
    private void infoLog(String messageToLog) {
        this.protocol.getLogger().info(messageToLog);
    }
}