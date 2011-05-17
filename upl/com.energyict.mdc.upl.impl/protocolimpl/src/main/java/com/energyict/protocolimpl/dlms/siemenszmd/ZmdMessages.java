package com.energyict.protocolimpl.dlms.siemenszmd;

import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocolimpl.dlms.DLMSZMD;
import com.energyict.protocolimpl.messages.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 12-mei-2011
 * Time: 11:36:59
 */
public class ZmdMessages extends ProtocolMessages {

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
            } else {
                infoLog("Unknown message received.");
                return MessageResult.createUnknown(messageEntry);
            }

        } catch (Exception e){
            infoLog("Message failed : " + e.getMessage());
        }
        return MessageResult.createFailed(messageEntry);
    }

    public List getMessageCategories() {
        List<MessageCategorySpec> categories = new ArrayList<MessageCategorySpec>();
        categories.add(ProtocolMessageCategories.getDemandResetCategory());
        return categories;
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
