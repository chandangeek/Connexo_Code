package com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.messaging;

import com.energyict.genericprotocolimpl.common.messages.GenericMessaging;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.messages.RtuMessageCategoryConstants;
import com.energyict.protocolimpl.messages.RtuMessageConstant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 25/01/12
 * Time: 15:04
 */
public class IskraMx372MbusMessaging  extends GenericMessaging implements MessageProtocol, PartialLoadProfileMessaging, LoadProfileRegisterMessaging {


    /**
     * Abstract method to define your message categories *
     */
    @Override
    public List getMessageCategories() {
        List messageCategories = new ArrayList();
        MessageCategorySpec cat = new MessageCategorySpec(RtuMessageCategoryConstants.BASICMESSAGES_DESCRIPTION);

        MessageSpec msgSpec = addBasicMsg("Connect meter", RtuMessageConstant.CONNECT_LOAD, false);
        cat.addMessageSpec(msgSpec);
        msgSpec = addBasicMsg("Disconnect meter", RtuMessageConstant.DISCONNECT_LOAD, false);
        cat.addMessageSpec(msgSpec);
        msgSpec = addMessageWithValue("Set vif to mbus device", RtuMessageConstant.MBUS_SET_VIF, false);
        cat.addMessageSpec(msgSpec);
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
     * At a later timestamp the framework will query each {@link com.energyict.protocol.MessageEntry} (see {@link #queryMessage(com.energyict.protocol.MessageEntry)}) to actually
     * perform the message.
     *
     * @param messageEntries a list of {@link com.energyict.protocol.MessageEntry}s
     * @throws java.io.IOException if a logical error occurs
     */
    public void applyMessages(List messageEntries) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
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

    public LoadProfileRegisterMessageBuilder getLoadProfileRegisterMessageBuilder() {
        return new LoadProfileRegisterMessageBuilder();
    }

    public PartialLoadProfileMessageBuilder getPartialLoadProfileMessageBuilder() {
        return new PartialLoadProfileMessageBuilder();
    }
}
