package com.energyict.protocolimpl.dlms.siemenszmd;

import com.energyict.mdc.protocol.api.UserFileFactory;
import com.energyict.mdc.protocol.api.codetables.CodeFactory;
import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.data.MessageResult;

import com.energyict.protocolimpl.base.ActivityCalendarController;
import com.energyict.protocolimpl.dlms.DLMSZMD;
import com.energyict.protocolimpl.messages.ProtocolMessageCategories;
import com.energyict.protocolimpl.messages.ProtocolMessages;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocols.messaging.TimeOfUseMessageBuilder;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 12-mei-2011
 * Time: 11:36:59
 */
public class ZmdMessages extends ProtocolMessages {

    private final DLMSZMD protocol;
    private final CodeFactory codeFactory;
    private final UserFileFactory userFileFactory;

    public ZmdMessages(final DLMSZMD protocol, CodeFactory codeFactory, UserFileFactory userFileFactory) {
        this.protocol = protocol;
        this.codeFactory = codeFactory;
        this.userFileFactory = userFileFactory;
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
        } catch (IOException e){
            infoLog("Message failed : " + e.getMessage());
        }    catch (SAXException e) {
             infoLog("Message failed - Unable to parse the ActivityCalendar upgrade message:" + e.getMessage());
        }
        return MessageResult.createFailed(messageEntry);
    }

    public List getMessageCategories() {
        return Arrays.asList(ProtocolMessageCategories.getDemandResetCategory());
    }

    private void updateTimeOfUse(MessageEntry messageEntry) throws IOException, SAXException {
        final ZMDTimeOfUseMessageBuilder builder = new ZMDTimeOfUseMessageBuilder(this.codeFactory, this.userFileFactory);
        ActivityCalendarController activityCalendarController = new ZMDActivityCalendarController(this.protocol);
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

    private void infoLog(String messageToLog) {
        this.protocol.getLogger().info(messageToLog);
    }

    }