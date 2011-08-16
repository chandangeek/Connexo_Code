package com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.ihd.messaging;

import com.energyict.genericprotocolimpl.common.messages.GenericMessaging;
import com.energyict.protocol.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Provides functionality to process messages for the InHomeDisplay
 */
public class InHomeDisplayMessaging extends GenericMessaging implements MessageProtocol{

    /**
     * Abstract method to define your message categories *
     */
    @Override
    public List getMessageCategories() {
        return Collections.emptyList();
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
        //TODO implement proper functionality.
    }

    /**
     * Indicates that each message has to be executed by the protocol.
     *
     * @param messageEntry a definition of which message needs to be sent
     * @return a state of the message which was just sent
     * @throws java.io.IOException if a logical error occurs
     */
    public MessageResult queryMessage(final MessageEntry messageEntry) throws IOException {
        return MessageResult.createFailed(messageEntry);
    }
}
