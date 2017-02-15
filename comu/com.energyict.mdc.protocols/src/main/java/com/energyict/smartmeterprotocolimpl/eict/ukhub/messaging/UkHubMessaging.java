/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.eict.ukhub.messaging;

import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.messaging.MessageAttributeSpec;
import com.energyict.mdc.protocol.api.messaging.MessageCategorySpec;
import com.energyict.mdc.protocol.api.messaging.MessageSpec;
import com.energyict.mdc.protocol.api.messaging.MessageTagSpec;
import com.energyict.mdc.protocol.api.messaging.MessageValueSpec;

import com.energyict.protocolimpl.generic.messages.GenericMessaging;
import com.energyict.protocolimpl.messages.ProtocolMessageCategories;
import com.energyict.protocolimpl.messages.RtuMessageCategoryConstants;
import com.energyict.protocolimpl.messages.RtuMessageConstant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UkHubMessaging extends GenericMessaging implements MessageProtocol {

    private final UkHubMessageExecutor messageExecutor;

    public UkHubMessaging(final UkHubMessageExecutor messageExecutor) {
        this.messageExecutor = messageExecutor;
    }

    /**
     * Abstract method to define your message categories *
     */
    @Override
    public List getMessageCategories() {
        List<MessageCategorySpec> categories = new ArrayList<>();
        categories.add(ProtocolMessageCategories.getHanManagementCategory());
        categories.add(getTestCategory());
        categories.add(getXmlConfigCategory());
        categories.add(getWebserverCategory());
        categories.add(getRebootCategory());
        categories.add(ProtocolMessageCategories.getGPRSModemCategory());
        categories.add(getFirmwareCategory());

        MessageCategorySpec logbookCategory = new MessageCategorySpec(RtuMessageCategoryConstants.LOGBOOK);
        logbookCategory.addMessageSpec(addMsgWithValuesAndOptionalValue("Read Debug Logbook", RtuMessageConstant.DEBUG_LOGBOOK, false, RtuMessageConstant.LOGBOOK_TO, RtuMessageConstant.LOGBOOK_FROM));
        logbookCategory.addMessageSpec(addMsgWithValuesAndOptionalValue("Read Elster Specific Logbook", RtuMessageConstant.ELSTER_SPECIFIC_LOGBOOK, false, RtuMessageConstant.LOGBOOK_TO, RtuMessageConstant.LOGBOOK_FROM));
        categories.add(logbookCategory);

        return categories;
    }

    protected MessageSpec addMsgWithValuesAndOptionalValue(final String description, final String tagName, final boolean advanced, String lastAttribute, String... attr) {
        MessageSpec msgSpec = new MessageSpec(description, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        for (String attribute : attr) {
            tagSpec.add(new MessageAttributeSpec(attribute, true));
        }
        tagSpec.add(new MessageAttributeSpec(lastAttribute, false));
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" "); //Disable this field
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    /**
     * Provides the full list of outstanding messages to the protocol.
     * If for any reason certain messages have to be grouped before they are sent to a device, then this is the place to do it.
     * At a later timestamp the framework will query each {@link MessageEntry} (see {@link #queryMessage(MessageEntry)}) to actually
     * perform the message.
     *
     * @param messageEntries a list of {@link MessageEntry}s
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

}