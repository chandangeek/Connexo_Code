/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.messaging;

import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.messaging.MessageCategorySpec;
import com.energyict.mdc.protocol.api.messaging.MessageSpec;
import com.energyict.mdc.protocol.api.messaging.MessageTagSpec;
import com.energyict.mdc.protocol.api.messaging.MessageValueSpec;
import com.energyict.protocols.messaging.LegacyLoadProfileRegisterMessageBuilder;
import com.energyict.protocols.messaging.LegacyPartialLoadProfileMessageBuilder;

import com.energyict.protocolimpl.generic.messages.GenericMessaging;
import com.energyict.protocolimpl.messages.RtuMessageCategoryConstants;
import com.energyict.protocolimpl.messages.RtuMessageConstant;

import java.io.IOException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

public class IskraMx372MbusMessaging  extends GenericMessaging implements MessageProtocol {

    private final Clock clock;
    private final TopologyService topologyService;
    private final LoadProfileFactory loadProfileFactory;

    public IskraMx372MbusMessaging(Clock clock, TopologyService topologyService, LoadProfileFactory loadProfileFactory) {
        this.clock = clock;
        this.topologyService = topologyService;
        this.loadProfileFactory = loadProfileFactory;
    }

    /**
     * Abstract method to define your message categories *
     */
    @Override
    public List getMessageCategories() {
        List<MessageCategorySpec> messageCategories = new ArrayList<>();
        MessageCategorySpec cat = new MessageCategorySpec(RtuMessageCategoryConstants.BASICMESSAGES_DESCRIPTION);
        cat.addMessageSpec(addBasicMsg("Connect meter", RtuMessageConstant.CONNECT_LOAD, false));
        cat.addMessageSpec(addBasicMsg("Disconnect meter", RtuMessageConstant.DISCONNECT_LOAD, false));
        cat.addMessageSpec(addMessageWithValue("Set vif to mbus device", RtuMessageConstant.MBUS_SET_VIF, false));
        messageCategories.add(cat);
        return messageCategories;
    }

    private MessageSpec addBasicMsg(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    private MessageSpec addMessageWithValue(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageValueSpec());
        msgSpec.add(tagSpec);
        return msgSpec;
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
    public void applyMessages(List messageEntries) throws IOException {
    }

    /**
     * Indicates that each message has to be executed by the protocol.
     *
     * @param messageEntry a definition of which message needs to be sent
     * @return a state of the message which was just sent
     * @throws java.io.IOException if a logical error occurs
     */
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        // The messages are handled in the IskraMx372MbusMessageExecutor, not here.
        return MessageResult.createFailed(messageEntry);
    }

    public LegacyLoadProfileRegisterMessageBuilder getLoadProfileRegisterMessageBuilder() {
        return new LegacyLoadProfileRegisterMessageBuilder(clock, this.topologyService, this.loadProfileFactory);
    }

    public LegacyPartialLoadProfileMessageBuilder getPartialLoadProfileMessageBuilder() {
        return new LegacyPartialLoadProfileMessageBuilder(clock, this.topologyService, this.loadProfileFactory);
    }

}