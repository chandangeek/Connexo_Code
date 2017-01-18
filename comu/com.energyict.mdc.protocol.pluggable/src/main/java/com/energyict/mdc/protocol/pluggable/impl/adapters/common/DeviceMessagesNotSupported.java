package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.messaging.Message;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of a {@link MessageProtocol} which does not support Messages
 *
 * @author gna
 * @since 10/04/12 - 15:11
 */
public class DeviceMessagesNotSupported implements MessageProtocol {

    public static final String NOT_SUPPORTED = "notSupported";

    /**
     * Provides the full list of outstanding messages to the protocol.
     * If for any reason certain messages have to be grouped before they are sent to a device, then this is the place to do it.
     * At a later timestamp the framework will query each {@link MessageEntry} (see {@link #queryMessage(MessageEntry)}) to actually
     * perform the message.
     *
     * @param messageEntries a list of {@link MessageEntry}s
     * @throws java.io.IOException if a logical error occurs
     */
    @Override
    public void applyMessages(final List messageEntries) throws IOException {
        throw new UnsupportedException("Messages are not supported");
    }

    /**
     * Indicates that each message has to be executed by the protocol.
     *
     * @param messageEntry a definition of which message needs to be sent
     * @return a state of the message which was just sent
     * @throws java.io.IOException if a logical error occurs
     */
    @Override
    public MessageResult queryMessage(final MessageEntry messageEntry) throws IOException {
        throw new UnsupportedException("Messages are not supported");
    }

    @Override
    public List getMessageCategories() {
        return Collections.emptyList();
    }

    @Override
    public String writeMessage(final Message msg) {
        return NOT_SUPPORTED;
    }

    @Override
    public String writeTag(final MessageTag tag) {
        return NOT_SUPPORTED;
    }

    @Override
    public String writeValue(final MessageValue value) {
        return NOT_SUPPORTED;
    }
}
