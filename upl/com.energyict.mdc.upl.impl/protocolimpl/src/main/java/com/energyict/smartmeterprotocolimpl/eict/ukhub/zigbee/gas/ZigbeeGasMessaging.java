package com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas;

import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.messages.ProtocolMessages;
import com.energyict.protocolimpl.utils.MessagingTools;
import sun.misc.MessageUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 20/07/11
 * Time: 16:55
 */
public class ZigbeeGasMessaging extends ProtocolMessages {

    public void applyMessages(List messageEntries) throws IOException {
        // Nothing to do here
    }

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        // Add messages here
        return MessageResult.createFailed(messageEntry);
    }

    public List getMessageCategories() {
        List<MessageCategorySpec> categorySpecs = new ArrayList<MessageCategorySpec>();
        // Add message categories here
        return categorySpecs;
    }

}
