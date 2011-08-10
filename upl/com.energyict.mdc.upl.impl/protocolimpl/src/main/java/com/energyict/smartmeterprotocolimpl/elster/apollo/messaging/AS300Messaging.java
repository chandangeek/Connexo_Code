package com.energyict.smartmeterprotocolimpl.elster.apollo.messaging;

import com.energyict.genericprotocolimpl.common.messages.GenericMessaging;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 8-aug-2011
 * Time: 14:32:04
 */
public class AS300Messaging extends GenericMessaging implements MessageProtocol, TimeOfUseMessaging {

    private final AS300MessageExecutor messageExecutor;

    public AS300Messaging(final AS300MessageExecutor messageExecutor) {
        this.messageExecutor = messageExecutor;
    }

    /**
     * Abstract method to define your message categories *
     */
    @Override
    public List getMessageCategories() {
        return new ArrayList<MessageCategorySpec>();
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
        //currently nothing to implement
    }

    /**
     * Indicates that each message has to be executed by the protocol.
     *
     * @param messageEntry a definition of which message needs to be sent
     * @return a state of the message which was just sent
     * @throws java.io.IOException if a logical error occurs
     */
    public MessageResult queryMessage(final MessageEntry messageEntry) throws IOException {
        return this.messageExecutor.executeMessageEntry(messageEntry);
    }

    /**
     * Indicates whether the protocol needs a 'name' for the tarif calendar or not.
     * For some meter a name needs to be sent to the meter.  If this is necessary the protocol must
     * return true for this method.
     *
     * @return <code>true</code> a 'name' is needed for the tarif calendar, <code>false</code> if not.
     */
    public boolean needsName() {
        return true;
    }

    /**
     * Indicates whether the tariff calendar data is saved in code table or not.
     *
     * @return <code>true</code> the tariff calendar data is saved in code table, <code>false</code> if not.
     */
    public boolean supportsCodeTables() {
        return true;
    }

    /**
     * Indicates whether the tarif calendar data is saved in a userfile or not.
     *
     * @return <code>true</code> the tarif calendar data is saved in a userfile, <code>false</code> if not.
     */
    public boolean supportsUserFiles() {
        return true;
    }

    /**
     * Indicates whether the implementer supports {@link com.energyict.mdw.core.UserFile} references. This will typically be used by generic protocols, as these have
     * access to the database. If {@link #supportsUserFiles()} is true, and this method returns <code>false</code>, the entire file is sent as payload in
     * the message. If this method returns <code>true</code>, a user file ID is passed on to the implementer, who can then query the database for the
     * contents of the file.
     *
     * @return <code>true</code> if the protocol supports a file ID reference, <code>false</code> if it does not. This can equally be translated into
     *         <code>true</code> for a generic protocol, and <code>false</code> for a normal one.
     */
    public boolean supportsUserFileReferences() {
        return false;   // false because we "don't" want access to the database
    }

    /**
     * Indicates whether the implementer supports {@link com.energyict.mdw.core.Code} references. This will typically be used by generic protocols, as these have
     * access to the database. If {@link #supportsCodeTables()} is true, and this method returns <code>false</code>, the entire file is sent as payload in
     * the message. If this method returns <code>true</code>, a user file ID is passed on to the implementer, who can then query the database for the
     * contents of the file.
     *
     * @return <code>true</code> if the protocol supports a codeTable ID reference, <code>false</code> if it does not. This can equally be translated into
     *         <code>true</code> for a generic protocol, and <code>false</code> for a normal one.
     */
    public boolean supportsCodeTableReferences() {
        return false;   // false because we "don't" want access to the database
    }

    /**
     * Indicates whether the content of the {@link com.energyict.mdw.core.UserFile} of the {@link com.energyict.mdw.core.Code} must be zipped when it is inlined in the
     * RtuMessage. This is only taken into account when {@link #supportsCodeTableReferences()} or {@link #supportsUserFileReferences()} is false.</br>
     * <b><u>NOTE:</u> If the content is zipped, then Base64 Encoding is also applied!</b>
     *
     * @return true if the content needs to be zipped, false otherwise
     */
    public boolean zipContent() {
        return true;
    }

    /**
     * Indicate whether the content of the {@link com.energyict.mdw.core.UserFile} or {@link com.energyict.mdw.core.Code} must be Base64 Encoded.
     * <b><u>NOTE:</u> Base64 encoding will be automatically applied if {@link #zipContent()} returns true</b>
     *
     * @return true if the content needs to be Base64 encoded, false otherwise
     */
    public boolean encodeContentToBase64() {
        return false;  // it gets B64 encoded with the ZIP file
    }

    /**
     * Returns the message builder capable of generating and parsing 'time of use' messages.
     *
     * @return The {@link com.energyict.protocol.messaging.MessageBuilder} capable of generating and parsing 'time of use' messages.
     */
    public TimeOfUseMessageBuilder getTimeOfUseMessageBuilder() {
        return new AS300TimeOfUseMessageBuilder();
    }
}
