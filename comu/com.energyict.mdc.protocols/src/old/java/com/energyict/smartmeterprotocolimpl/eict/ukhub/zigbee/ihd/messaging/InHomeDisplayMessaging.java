package com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.ihd.messaging;

import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.messaging.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.protocolimpl.generic.messages.GenericMessaging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides functionality to process messages for the InHomeDisplay
 */
public class InHomeDisplayMessaging extends GenericMessaging implements MessageProtocol{

    private InHomeDisplayMessageExecutor messageExecutor;

    public InHomeDisplayMessaging(InHomeDisplayMessageExecutor messageExecutor) {
        this.messageExecutor = messageExecutor;
    }

    public void applyMessages(final List messageEntries) throws IOException {
        // nothing to do here
    }

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return this.messageExecutor.queryMessage(messageEntry);
    }

    @Override
    public List getMessageCategories() {
        List<MessageCategorySpec> categories = new ArrayList<>();
        categories.add(getFirmwareCategory());
        return categories;
    }

}