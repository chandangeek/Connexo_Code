package com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.ihd.messaging;

import com.energyict.genericprotocolimpl.common.messages.GenericMessaging;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.MessageCategorySpec;

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

    /**
     * Abstract method to define your message categories *
     */
    @Override
    public List getMessageCategories() {
        List<MessageCategorySpec> categories = new ArrayList<MessageCategorySpec>();
        categories.add(getFirmwareCategory());
        return categories;
    }
}