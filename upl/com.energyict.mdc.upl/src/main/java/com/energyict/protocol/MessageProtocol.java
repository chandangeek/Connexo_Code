/*
 * MessageProtocol.java
 *
 * Created on 27 juli 2007, 15:13
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocol;

import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.Messaging;

import java.io.IOException;
import java.util.List;

/**
 * Provides functionality to send {@link com.energyict.mdw.core.OldDeviceMessage}s to a device.
 *
 * @author kvds
 */
public interface MessageProtocol extends Messaging {

    /**
     * Provides the full list of outstanding messages to the protocol.
     * If for any reason certain messages have to be grouped before they are sent to a device, then this is the place to do it.
     * At a later timestamp the framework will query each {@link MessageEntry} (see {@link #queryMessage(MessageEntry)}) to actually
     * perform the message.
     *
     * @param messageEntries a list of {@link MessageEntry}s
     * @throws IOException if a logical error occurs
     */
    void applyMessages(List messageEntries) throws IOException;

    /**
     * Indicates that each message has to be executed by the protocol.
     *
     * @param messageEntry a definition of which message needs to be sent
     * @return a state of the message which was just sent
     * @throws IOException if a logical error occurs
     */
    MessageResult queryMessage(MessageEntry messageEntry) throws IOException;

}
