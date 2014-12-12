package com.energyict.smartmeterprotocolimpl.eict.webrtuz3.messaging;

import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.protocolimpl.generic.messages.GenericMessaging;
import com.energyict.mdc.protocol.api.device.data.*;
import com.energyict.mdc.protocol.api.messaging.MessageCategorySpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 29-aug-2011
 * Time: 15:41:26
 */
public class MbusDeviceMessaging extends GenericMessaging implements MessageProtocol {

    /**
     * Abstract method to define your message categories *
     */
    @Override
	public List getMessageCategories() {
		List<MessageCategorySpec> categories = new ArrayList();
		MessageCategorySpec catDisconnect = getConnectControlCategory();
		MessageCategorySpec catMbusSetup = getMbusSetupCategory();

		categories.add(catDisconnect);
		categories.add(catMbusSetup);
		return categories;
	}

    /**
     * Provides the full list of outstanding messages to the protocol.
     * If for any reason certain messages have to be grouped before they are sent to a device, then this is the place to do it.
     * At a later timestamp the framework will query each MessageEntry (see #queryMessage(MessageEntry)) to actually
     * perform the message.
     *
     * @param messageEntries a list of MessageEntrys
     * @throws java.io.IOException if a logical error occurs
     */
    public void applyMessages(final List messageEntries) throws IOException {
        // nothing to do
    }

    /**
     * Indicates that each message has to be executed by the protocol.
     *
     * @param messageEntry a definition of which message needs to be sent
     * @return a state of the message which was just sent
     * @throws java.io.IOException if a logical error occurs
     */
    public MessageResult queryMessage(final MessageEntry messageEntry) throws IOException {
        return null;
    }
}
