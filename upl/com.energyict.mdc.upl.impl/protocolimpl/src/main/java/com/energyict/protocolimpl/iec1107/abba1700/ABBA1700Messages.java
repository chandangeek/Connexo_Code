package com.energyict.protocolimpl.iec1107.abba1700;

import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.messages.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides straightforward functionality to handle meterMessages
 */
public class ABBA1700Messages extends ProtocolMessages {

    private final ABBA1700 protocol;

    public ABBA1700Messages(final ABBA1700 protocol) {
        this.protocol = protocol;
    }

    public List getMessageCategories() {
        List<MessageCategorySpec> categories = new ArrayList();
        categories.add(ProtocolMessageCategories.getDemandResetCategory());
        return categories;
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
        if (isItThisMessage(messageEntry, RtuMessageConstant.DEMAND_RESET)) {
            infoLog("Sending message DemandReset.");
            this.protocol.resetDemand();
            infoLog("DemandReset message successful.");
            return MessageResult.createSuccess(messageEntry);
        }
        return MessageResult.createUnknown(messageEntry);
    }

    /**
     * Log the given message to the logger with the INFO level
     * @param messageToLog
     */
    private void infoLog(String messageToLog){
        this.protocol.getLogger().info(messageToLog);
    }
}
