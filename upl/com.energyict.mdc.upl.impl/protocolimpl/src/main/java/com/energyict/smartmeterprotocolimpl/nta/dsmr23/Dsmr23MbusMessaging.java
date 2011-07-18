package com.energyict.smartmeterprotocolimpl.nta.dsmr23;

import com.energyict.genericprotocolimpl.common.messages.GenericMessaging;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 15-jul-2011
 * Time: 13:23:56
 */
public class Dsmr23MbusMessaging extends GenericMessaging implements MessageProtocol{

    private final Dsmr23MbusMessageExecutor messageExecutor;

    public Dsmr23MbusMessaging(final Dsmr23MbusMessageExecutor messageExecutor) {
        this.messageExecutor = messageExecutor;
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
        //nothing much to do here ...
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

    public List getMessageCategories() {
		List<MessageCategorySpec> categories = new ArrayList<MessageCategorySpec>();
		MessageCategorySpec catDisconnect = getConnectControlCategory();
		MessageCategorySpec catMbusSetup = getMbusSetupCategory();

		categories.add(catDisconnect);
		categories.add(catMbusSetup);
		return categories;
    }
}
