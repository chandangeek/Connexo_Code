package com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas;

import com.energyict.genericprotocolimpl.common.messages.GenericMessaging;
import com.energyict.mdw.core.*;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.messages.ProtocolMessages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 20/07/11
 * Time: 16:55
 */
public class ZigbeeGasMessaging extends GenericMessaging implements TimeOfUseMessaging {

    private TimeOfUseMessageBuilder messageBuilder = null;

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

    public boolean needsName() {
        return true;
    }

    public boolean supportsCodeTables() {
        return true;
    }

    public boolean supportsUserFiles() {
        return false;
    }

    public TimeOfUseMessageBuilder getTimeOfUseMessageBuilder() {
        if (messageBuilder == null) {
            this.messageBuilder = new TimeOfUseMessageBuilder();
        }
        return messageBuilder;
    }

}
